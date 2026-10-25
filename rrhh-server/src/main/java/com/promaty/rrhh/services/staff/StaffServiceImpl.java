package com.promaty.rrhh.services.staff;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.dto.staff.StaffListDto;
import com.promaty.rrhh.dto.staff.StaffSelectorOptionDto;
import com.promaty.rrhh.dto.staff.StaffTerminationSelectorOptionDto;
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.dto.staff.UpdateStaffStatusDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.projection.StaffActiveContractDateRangeProjection;
import com.promaty.rrhh.repository.projection.StaffContractStartDateProjection;
import com.promaty.rrhh.repository.projection.StaffCostCenterProjection;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.staff.business.builder.CreateStaffBuilder;
import com.promaty.rrhh.services.staff.business.builder.StaffQueryBuilder;
import com.promaty.rrhh.services.staff.business.builder.StaffRelationsResolver;
import com.promaty.rrhh.services.staff.business.builder.UpdateStaffBuilder;
import com.promaty.rrhh.services.staff.business.mapper.StaffMapper;
import com.promaty.rrhh.services.staff.business.validation.StaffValidation;

@Service
public class StaffServiceImpl implements StaffService {

	private static final String NO_ENCONTRADO = "Colaborador no encontrado.";
	private static final String ESTADO_ACTIVO = "ACTIVE";
	private static final String CODIGO_ESTADO_BLOCKED = "BLOCKED";
	private static final List<String> ESTADOS_CONTRATO_ALGUNA_VEZ_ACTIVO =
		List.of("ACTIVE", "EXPIRED", "ANNULLED", "TERMINATED");

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"staff.update", Action.UPDATE,
		"staff.status", Action.STATUS
	);

	private final StaffRepository staffRepository;
	private final ContractRepository contractRepository;
	private final StaffValidation staffValidation;
	private final CreateStaffBuilder createStaffBuilder;
	private final UpdateStaffBuilder updateStaffBuilder;
	private final StaffRelationsResolver relationsResolver;
	private final ActionsResolver actionsResolver;

	public StaffServiceImpl(
		StaffRepository staffRepository,
		ContractRepository contractRepository,
		StaffValidation staffValidation,
		CreateStaffBuilder createStaffBuilder,
		UpdateStaffBuilder updateStaffBuilder,
		StaffRelationsResolver relationsResolver,
		ActionsResolver actionsResolver
	) {
		this.staffRepository = staffRepository;
		this.contractRepository = contractRepository;
		this.staffValidation = staffValidation;
		this.createStaffBuilder = createStaffBuilder;
		this.updateStaffBuilder = updateStaffBuilder;
		this.relationsResolver = relationsResolver;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createStaff(CreateStaffDto dto) {
		staffValidation.validateCreate(dto);
		Staff staff = createStaffBuilder.build(dto);
		return staffRepository.save(staff).getId();
	}

	@Override
	@Transactional
	public void updateStaff(Long id, UpdateStaffDto dto) {
		staffValidation.validateUpdate(dto);
		Staff existente = buscarPorId(id);
		updateStaffBuilder.apply(existente, dto);
		staffRepository.save(existente);
	}

	@Override
	@Transactional
	public StaffDetailDto updateStaffStatus(Long id, UpdateStaffStatusDto dto) {
		staffValidation.validateStatusChange(dto);
		Staff existente = buscarPorId(id);
		existente.setStatus(relationsResolver.resolveStatus(dto.getStatusId()));
		existente.setBlockReason(CODIGO_ESTADO_BLOCKED.equals(existente.getStatus().getCode()) ? dto.getBlockReason() : null);
		return conAcciones(StaffMapper.toDetailDto(staffRepository.save(existente), resolverCentroCosto(id), resolverFechaIngreso(id)));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<StaffListDto> listStaff(StaffFilterParams filters, Pageable pageable) {
		Specification<Staff> especificacion = StaffQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		Page<Staff> pagina = staffRepository.findAll(especificacion, pageable);

		List<Long> staffIds = pagina.getContent().stream().map(Staff::getId).toList();
		Map<Long, String> centrosCostoPorStaff = resolverCentrosCostoEnLote(staffIds);
		Map<Long, LocalDate> fechasIngresoPorStaff = resolverFechasIngresoEnLote(staffIds);

		return pagina
			.map(staff -> StaffMapper.toListDto(
				staff,
				centrosCostoPorStaff.get(staff.getId()),
				fechasIngresoPorStaff.get(staff.getId())
			))
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public StaffDetailDto getStaffDetail(Long id) {
		Staff staff = buscarPorId(id);
		return conAcciones(StaffMapper.toDetailDto(staff, resolverCentroCosto(id), resolverFechaIngreso(id)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<StaffSelectorOptionDto> listSelectorOptionsForContract() {
		return staffRepository.findAvailableForContractSelector(ESTADO_ACTIVO, CODIGO_ESTADO_BLOCKED)
			.stream()
			.map(StaffMapper::toSelectorOptionDto)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<StaffTerminationSelectorOptionDto> listSelectorOptionsForTermination() {
		List<Staff> staffConContratoActivo = staffRepository.findWithActiveContractForTerminationSelector(ESTADO_ACTIVO);
		List<Long> staffIds = staffConContratoActivo.stream().map(Staff::getId).toList();
		Map<Long, StaffActiveContractDateRangeProjection> rangosPorStaff = contractRepository
			.findActiveContractDateRangesByStaffIds(staffIds, ESTADO_ACTIVO)
			.stream()
			.collect(Collectors.toMap(StaffActiveContractDateRangeProjection::getStaffId, p -> p));

		return staffConContratoActivo.stream()
			.map(staff -> StaffMapper.toTerminationSelectorOptionDto(staff, rangosPorStaff.get(staff.getId())))
			.toList();
	}

	private String resolverCentroCosto(Long staffId) {
		return contractRepository.findByStaffIdAndStatus_Code(staffId, ESTADO_ACTIVO)
			.map(Contract::getProject)
			.map(Project::getCostCenterCode)
			.orElse(null);
	}

	private LocalDate resolverFechaIngreso(Long staffId) {
		return contractRepository
			.findFirstByStaffIdAndStatus_CodeInOrderByStartDateDesc(staffId, ESTADOS_CONTRATO_ALGUNA_VEZ_ACTIVO)
			.map(Contract::getStartDate)
			.orElse(null);
	}

	private Map<Long, String> resolverCentrosCostoEnLote(List<Long> staffIds) {
		if (staffIds.isEmpty()) {
			return Map.of();
		}
		return contractRepository.findActiveCostCentersByStaffIds(staffIds, ESTADO_ACTIVO).stream()
			.collect(Collectors.toMap(StaffCostCenterProjection::getStaffId, StaffCostCenterProjection::getCostCenterCode));
	}

	private Map<Long, LocalDate> resolverFechasIngresoEnLote(List<Long> staffIds) {
		if (staffIds.isEmpty()) {
			return Map.of();
		}
		return contractRepository.findStartDatesByStaffIds(staffIds, ESTADOS_CONTRATO_ALGUNA_VEZ_ACTIVO).stream()
			.collect(Collectors.toMap(
				StaffContractStartDateProjection::getStaffId,
				StaffContractStartDateProjection::getStartDate,
				BinaryOperator.maxBy(Comparator.naturalOrder())
			));
	}

	private Staff buscarPorId(Long id) {
		return staffRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private StaffListDto conAcciones(StaffListDto dto, List<Action> actions) {
		dto.setActions(actions);
		return dto;
	}

	private StaffDetailDto conAcciones(StaffDetailDto dto) {
		dto.setActions(actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES));
		return dto;
	}
}
