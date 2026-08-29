package com.promaty.rrhh.services.projecttype;

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
import com.promaty.rrhh.entity.ProjectType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ProjectTypeRepository;
import com.promaty.rrhh.services.projecttype.business.builder.ProjectTypeQueryBuilder;
import com.promaty.rrhh.services.projecttype.business.mapper.ProjectTypeMapper;
import com.promaty.rrhh.services.projecttype.business.validation.ProjectTypeValidation;

@Service
public class ProjectTypeServiceImpl implements ProjectTypeService {

	private static final String NO_ENCONTRADO = "Tipo de proyecto no encontrado.";

	private final ProjectTypeRepository projectTypeRepository;
	private final ProjectTypeValidation projectTypeValidation;

	public ProjectTypeServiceImpl(ProjectTypeRepository projectTypeRepository, ProjectTypeValidation projectTypeValidation) {
		this.projectTypeRepository = projectTypeRepository;
		this.projectTypeValidation = projectTypeValidation;
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
		return projectTypeRepository.findAll(especificacion, pageable).map(ProjectTypeMapper::toListDto);
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectTypeDetailDto getProjectTypeDetail(Long id) {
		return ProjectTypeMapper.toDetailDto(buscarPorId(id));
	}

	@Override
	@Transactional
	public ProjectTypeDetailDto toggleProjectTypeActive(Long id) {
		ProjectType projectType = buscarPorId(id);
		projectType.toggleActive();
		return ProjectTypeMapper.toDetailDto(projectTypeRepository.save(projectType));
	}

	private ProjectType buscarPorId(Long id) {
		return projectTypeRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}
}
