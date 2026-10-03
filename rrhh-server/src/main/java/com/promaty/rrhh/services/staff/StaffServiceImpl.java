package com.promaty.rrhh.services.staff;

import java.util.List;
import java.util.Map;

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
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.staff.business.builder.CreateStaffBuilder;
import com.promaty.rrhh.services.staff.business.builder.StaffQueryBuilder;
import com.promaty.rrhh.services.staff.business.builder.UpdateStaffBuilder;
import com.promaty.rrhh.services.staff.business.mapper.StaffMapper;
import com.promaty.rrhh.services.staff.business.validation.StaffValidation;

@Service
public class StaffServiceImpl implements StaffService {

	private static final String NO_ENCONTRADO = "Colaborador no encontrado.";
	private static final String ESTADO_ACTIVO = "ACTIVE";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"staff.update", Action.UPDATE
	);

	private final StaffRepository staffRepository;
	private final ContractRepository contractRepository;
	private final StaffValidation staffValidation;
	private final CreateStaffBuilder createStaffBuilder;
	private final UpdateStaffBuilder updateStaffBuilder;
	private final ActionsResolver actionsResolver;

	public StaffServiceImpl(
		StaffRepository staffRepository,
		ContractRepository contractRepository,
		StaffValidation staffValidation,
		CreateStaffBuilder createStaffBuilder,
		UpdateStaffBuilder updateStaffBuilder,
		ActionsResolver actionsResolver
	) {
		this.staffRepository = staffRepository;
		this.contractRepository = contractRepository;
		this.staffValidation = staffValidation;
		this.createStaffBuilder = createStaffBuilder;
		this.updateStaffBuilder = updateStaffBuilder;
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
	@Transactional(readOnly = true)
	public Page<StaffListDto> listStaff(StaffFilterParams filters, Pageable pageable) {
		Specification<Staff> especificacion = StaffQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		return staffRepository.findAll(especificacion, pageable)
			.map(staff -> StaffMapper.toListDto(staff, resolverCentroCosto(staff.getId())))
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public StaffDetailDto getStaffDetail(Long id) {
		Staff staff = buscarPorId(id);
		return conAcciones(StaffMapper.toDetailDto(staff, resolverCentroCosto(id)));
	}

	private String resolverCentroCosto(Long staffId) {
		return contractRepository.findByStaffIdAndStatus_Code(staffId, ESTADO_ACTIVO)
			.map(Contract::getProject)
			.map(Project::getCostCenterCode)
			.orElse(null);
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
