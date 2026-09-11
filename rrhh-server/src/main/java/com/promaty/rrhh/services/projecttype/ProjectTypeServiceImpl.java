package com.promaty.rrhh.services.projecttype;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.projecttype.CreateProjectTypeDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeDetailDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeFilterParams;
import com.promaty.rrhh.dto.projecttype.ProjectTypeListDto;
import com.promaty.rrhh.dto.projecttype.UpdateProjectTypeDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.ProjectType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ProjectTypeRepository;
import com.promaty.rrhh.services.projecttype.business.builder.ProjectTypeQueryBuilder;
import com.promaty.rrhh.services.projecttype.business.mapper.ProjectTypeMapper;
import com.promaty.rrhh.services.projecttype.business.validation.ProjectTypeValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;

@Service
public class ProjectTypeServiceImpl implements ProjectTypeService {

	private static final String NO_ENCONTRADO = "Tipo de proyecto no encontrado.";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"projectType.update", Action.UPDATE,
		"projectType.active", Action.ACTIVE
	);

	private final ProjectTypeRepository projectTypeRepository;
	private final ProjectTypeValidation projectTypeValidation;
	private final ActionsResolver actionsResolver;

	public ProjectTypeServiceImpl(ProjectTypeRepository projectTypeRepository, ProjectTypeValidation projectTypeValidation,
			ActionsResolver actionsResolver) {
		this.projectTypeRepository = projectTypeRepository;
		this.projectTypeValidation = projectTypeValidation;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createProjectType(CreateProjectTypeDto dto) {
		projectTypeValidation.validateCreate(dto);
		ProjectType projectType = new ProjectType();
		projectType.setName(dto.getName());
		return projectTypeRepository.save(projectType).getId();
	}

	@Override
	@Transactional
	public void updateProjectType(Long id, UpdateProjectTypeDto dto) {
		projectTypeValidation.validateUpdate(id, dto);
		ProjectType existente = buscarPorId(id);
		existente.setName(dto.getName());
		projectTypeRepository.save(existente);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProjectTypeListDto> listProjectTypes(ProjectTypeFilterParams filters, Pageable pageable) {
		Specification<ProjectType> especificacion = ProjectTypeQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		return projectTypeRepository.findAll(especificacion, pageable)
			.map(ProjectTypeMapper::toListDto)
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectTypeDetailDto getProjectTypeDetail(Long id) {
		return conAcciones(ProjectTypeMapper.toDetailDto(buscarPorId(id)));
	}

	@Override
	@Transactional
	public ProjectTypeDetailDto toggleProjectTypeActive(Long id) {
		ProjectType projectType = buscarPorId(id);
		projectType.toggleActive();
		return conAcciones(ProjectTypeMapper.toDetailDto(projectTypeRepository.save(projectType)));
	}

	private ProjectType buscarPorId(Long id) {
		return projectTypeRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private ProjectTypeListDto conAcciones(ProjectTypeListDto dto, List<Action> actions) {
		dto.setActions(actions);
		return dto;
	}

	private ProjectTypeDetailDto conAcciones(ProjectTypeDetailDto dto) {
		dto.setActions(actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES));
		return dto;
	}
}
