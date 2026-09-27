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
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.staff.business.builder.StaffQueryBuilder;
import com.promaty.rrhh.services.staff.business.mapper.StaffMapper;
import com.promaty.rrhh.services.staff.business.validation.StaffValidation;

@Service
public class StaffServiceImpl implements StaffService {

	private static final String NO_ENCONTRADO = "Colaborador no encontrado.";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"staff.update", Action.UPDATE
	);

	private final StaffRepository staffRepository;
	private final StaffValidation staffValidation;
	private final ActionsResolver actionsResolver;

	public StaffServiceImpl(
		StaffRepository staffRepository,
		StaffValidation staffValidation,
		ActionsResolver actionsResolver
	) {
		this.staffRepository = staffRepository;
		this.staffValidation = staffValidation;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createStaff(CreateStaffDto dto) {
		staffValidation.validateCreate(dto);

		Staff staff = new Staff();
		staff.setIdentificationType(dto.getIdentificationType());
		staff.setIdentificationNumber(dto.getIdentificationNumber());
		staff.setFirstName(dto.getFirstName());
		staff.setPaternalLastName(dto.getPaternalLastName());
		staff.setMaternalLastName(dto.getMaternalLastName());
		staff.setBirthDate(dto.getBirthDate());
		staff.setPersonalEmail(dto.getPersonalEmail());
		staff.setPhone1(dto.getPhone1());

		return staffRepository.save(staff).getId();
	}

	@Override
	@Transactional
	public void updateStaff(Long id, UpdateStaffDto dto) {
		staffValidation.validateUpdate(dto);
		Staff existente = buscarPorId(id);

		existente.setFirstName(dto.getFirstName());
		existente.setPaternalLastName(dto.getPaternalLastName());
		existente.setMaternalLastName(dto.getMaternalLastName());
		existente.setBirthDate(dto.getBirthDate());
		existente.setPersonalEmail(dto.getPersonalEmail());
		existente.setPhone1(dto.getPhone1());

		staffRepository.save(existente);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<StaffListDto> listStaff(StaffFilterParams filters, Pageable pageable) {
		Specification<Staff> especificacion = StaffQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		return staffRepository.findAll(especificacion, pageable)
			.map(StaffMapper::toListDto)
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public StaffDetailDto getStaffDetail(Long id) {
		return conAcciones(StaffMapper.toDetailDto(buscarPorId(id)));
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
