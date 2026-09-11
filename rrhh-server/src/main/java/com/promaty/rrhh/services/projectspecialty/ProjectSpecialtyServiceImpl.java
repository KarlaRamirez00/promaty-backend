package com.promaty.rrhh.services.projectspecialty;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.projectspecialty.CreateProjectSpecialtyDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyDetailDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyFilterParams;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyListDto;
import com.promaty.rrhh.dto.projectspecialty.UpdateProjectSpecialtyDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.ProjectSpecialty;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;
import com.promaty.rrhh.services.projectspecialty.business.builder.ProjectSpecialtyQueryBuilder;
import com.promaty.rrhh.services.projectspecialty.business.mapper.ProjectSpecialtyMapper;
import com.promaty.rrhh.services.projectspecialty.business.validation.ProjectSpecialtyValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;

@Service
public class ProjectSpecialtyServiceImpl implements ProjectSpecialtyService {

	private static final String NO_ENCONTRADO = "Especialidad no encontrada.";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"projectSpecialty.update", Action.UPDATE,
		"projectSpecialty.active", Action.ACTIVE
	);

	private final ProjectSpecialtyRepository projectSpecialtyRepository;
	private final ProjectSpecialtyValidation projectSpecialtyValidation;
	private final ActionsResolver actionsResolver;

	public ProjectSpecialtyServiceImpl(ProjectSpecialtyRepository projectSpecialtyRepository,
			ProjectSpecialtyValidation projectSpecialtyValidation, ActionsResolver actionsResolver) {
		this.projectSpecialtyRepository = projectSpecialtyRepository;
		this.projectSpecialtyValidation = projectSpecialtyValidation;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createProjectSpecialty(CreateProjectSpecialtyDto dto) {
		projectSpecialtyValidation.validateCreate(dto);
		ProjectSpecialty projectSpecialty = new ProjectSpecialty();
		projectSpecialty.setName(dto.getName());
		return projectSpecialtyRepository.save(projectSpecialty).getId();
	}

	@Override
	@Transactional
	public void updateProjectSpecialty(Long id, UpdateProjectSpecialtyDto dto) {
		projectSpecialtyValidation.validateUpdate(id, dto);
		ProjectSpecialty existente = buscarPorId(id);
		existente.setName(dto.getName());
		projectSpecialtyRepository.save(existente);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProjectSpecialtyListDto> listProjectSpecialties(ProjectSpecialtyFilterParams filters, Pageable pageable) {
		Specification<ProjectSpecialty> especificacion = ProjectSpecialtyQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		return projectSpecialtyRepository.findAll(especificacion, pageable)
			.map(ProjectSpecialtyMapper::toListDto)
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectSpecialtyDetailDto getProjectSpecialtyDetail(Long id) {
		return conAcciones(ProjectSpecialtyMapper.toDetailDto(buscarPorId(id)));
	}

	@Override
	@Transactional
	public ProjectSpecialtyDetailDto toggleProjectSpecialtyActive(Long id) {
		ProjectSpecialty projectSpecialty = buscarPorId(id);
		projectSpecialty.toggleActive();
		return conAcciones(ProjectSpecialtyMapper.toDetailDto(projectSpecialtyRepository.save(projectSpecialty)));
	}

	private ProjectSpecialty buscarPorId(Long id) {
		return projectSpecialtyRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private ProjectSpecialtyListDto conAcciones(ProjectSpecialtyListDto dto, List<Action> actions) {
		dto.setActions(actions);
		return dto;
	}

	private ProjectSpecialtyDetailDto conAcciones(ProjectSpecialtyDetailDto dto) {
		dto.setActions(actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES));
		return dto;
	}
}
