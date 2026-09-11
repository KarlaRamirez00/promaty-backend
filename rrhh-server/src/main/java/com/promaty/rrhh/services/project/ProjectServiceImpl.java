package com.promaty.rrhh.services.project;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.project.CreateProjectDto;
import com.promaty.rrhh.dto.project.ProjectDetailDto;
import com.promaty.rrhh.dto.project.ProjectFilterParams;
import com.promaty.rrhh.dto.project.ProjectListDto;
import com.promaty.rrhh.dto.project.UpdateProjectDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.services.project.business.builder.CreateProjectBuilder;
import com.promaty.rrhh.services.project.business.builder.ProjectQueryBuilder;
import com.promaty.rrhh.services.project.business.builder.UpdateProjectBuilder;
import com.promaty.rrhh.services.project.business.mapper.ProjectMapper;
import com.promaty.rrhh.services.project.business.validation.ProjectValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;

@Service
public class ProjectServiceImpl implements ProjectService {

	private static final String NO_ENCONTRADO = "Proyecto no encontrado.";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"project.update", Action.UPDATE
	);

	private final ProjectRepository projectRepository;
	private final ProjectValidation projectValidation;
	private final CreateProjectBuilder createProjectBuilder;
	private final UpdateProjectBuilder updateProjectBuilder;
	private final ActionsResolver actionsResolver;

	public ProjectServiceImpl(
		ProjectRepository projectRepository,
		ProjectValidation projectValidation,
		CreateProjectBuilder createProjectBuilder,
		UpdateProjectBuilder updateProjectBuilder,
		ActionsResolver actionsResolver
	) {
		this.projectRepository = projectRepository;
		this.projectValidation = projectValidation;
		this.createProjectBuilder = createProjectBuilder;
		this.updateProjectBuilder = updateProjectBuilder;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createProject(CreateProjectDto dto) {
		projectValidation.validateCreate(dto);
		Project project = createProjectBuilder.build(dto);
		return projectRepository.save(project).getId();
	}

	@Override
	@Transactional
	public void updateProject(Long id, UpdateProjectDto dto) {
		projectValidation.validateUpdate(id, dto);
		Project existente = buscarPorId(id);
		updateProjectBuilder.apply(existente, dto);
		projectRepository.save(existente);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ProjectListDto> listProjects(ProjectFilterParams filters, Pageable pageable) {
		Specification<Project> especificacion = ProjectQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		return projectRepository.findAll(especificacion, pageable)
			.map(ProjectMapper::toListDto)
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectDetailDto getProjectDetail(Long id) {
		return conAcciones(ProjectMapper.toDetailDto(buscarPorId(id)));
	}

	private Project buscarPorId(Long id) {
		return projectRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private ProjectListDto conAcciones(ProjectListDto dto, List<Action> actions) {
		dto.setActions(actions);
		return dto;
	}

	private ProjectDetailDto conAcciones(ProjectDetailDto dto) {
		dto.setActions(actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES));
		return dto;
	}
}
