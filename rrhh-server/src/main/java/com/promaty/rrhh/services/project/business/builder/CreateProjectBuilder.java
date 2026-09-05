package com.promaty.rrhh.services.project.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.project.CreateProjectDto;
import com.promaty.rrhh.entity.Project;

@Component
public class CreateProjectBuilder {

	private final ProjectRelationsResolver relationsResolver;

	public CreateProjectBuilder(ProjectRelationsResolver relationsResolver) {
		this.relationsResolver = relationsResolver;
	}

	public Project build(CreateProjectDto dto) {
		Project project = new Project();
		project.setName(dto.getName());
		project.setCostCenterCode(dto.getCostCenterCode());
		project.setType(relationsResolver.resolveType(dto.getTypeId()));
		project.setSpecialty(relationsResolver.resolveSpecialty(dto.getSpecialtyId()));
		project.setClient(relationsResolver.resolveClient(dto.getClientId()));
		project.setStatus(relationsResolver.resolveStatus(dto.getStatusId()));
		project.setStartDate(dto.getStartDate());
		project.setEndDate(dto.getEndDate());
		return project;
	}
}
