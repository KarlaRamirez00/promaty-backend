package com.promaty.rrhh.services.project.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.project.UpdateProjectDto;
import com.promaty.rrhh.entity.Project;

@Component
public class UpdateProjectBuilder {

	private final ProjectRelationsResolver relationsResolver;

	public UpdateProjectBuilder(ProjectRelationsResolver relationsResolver) {
		this.relationsResolver = relationsResolver;
	}

	public void apply(Project project, UpdateProjectDto dto) {
		project.setName(dto.getName());
		project.setCostCenterCode(dto.getCostCenterCode());
		project.setType(relationsResolver.resolveType(dto.getTypeId()));
		project.setSpecialty(relationsResolver.resolveSpecialty(dto.getSpecialtyId()));
		project.setClient(relationsResolver.resolveClient(dto.getClientId()));
		project.setStartDate(dto.getStartDate());
		project.setEndDate(dto.getEndDate());
	}
}
