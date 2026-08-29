package com.promaty.rrhh.services.projectspecialty;

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
import com.promaty.rrhh.entity.ProjectSpecialty;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;
import com.promaty.rrhh.services.projectspecialty.business.builder.ProjectSpecialtyQueryBuilder;
import com.promaty.rrhh.services.projectspecialty.business.mapper.ProjectSpecialtyMapper;
import com.promaty.rrhh.services.projectspecialty.business.validation.ProjectSpecialtyValidation;

@Service
public class ProjectSpecialtyServiceImpl implements ProjectSpecialtyService {

	private static final String NO_ENCONTRADO = "Especialidad no encontrada.";

	private final ProjectSpecialtyRepository projectSpecialtyRepository;
	private final ProjectSpecialtyValidation projectSpecialtyValidation;

	public ProjectSpecialtyServiceImpl(ProjectSpecialtyRepository projectSpecialtyRepository, ProjectSpecialtyValidation projectSpecialtyValidation) {
		this.projectSpecialtyRepository = projectSpecialtyRepository;
		this.projectSpecialtyValidation = projectSpecialtyValidation;
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
		return projectSpecialtyRepository.findAll(especificacion, pageable).map(ProjectSpecialtyMapper::toListDto);
	}

	@Override
	@Transactional(readOnly = true)
	public ProjectSpecialtyDetailDto getProjectSpecialtyDetail(Long id) {
		return ProjectSpecialtyMapper.toDetailDto(buscarPorId(id));
	}

	@Override
	@Transactional
	public ProjectSpecialtyDetailDto toggleProjectSpecialtyActive(Long id) {
		ProjectSpecialty projectSpecialty = buscarPorId(id);
		projectSpecialty.toggleActive();
		return ProjectSpecialtyMapper.toDetailDto(projectSpecialtyRepository.save(projectSpecialty));
	}

	private ProjectSpecialty buscarPorId(Long id) {
		return projectSpecialtyRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}
}
