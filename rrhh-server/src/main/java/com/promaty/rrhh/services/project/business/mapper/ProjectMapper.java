package com.promaty.rrhh.services.project.business.mapper;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.dto.project.ProjectDetailDto;
import com.promaty.rrhh.dto.project.ProjectListDto;
import com.promaty.rrhh.dto.project.RelationSummaryDto;
import com.promaty.rrhh.entity.Project;

public final class ProjectMapper {

	private ProjectMapper() {
	}

	public static ProjectListDto toListDto(Project project) {
		return new ProjectListDto(
			project.getId(),
			project.getName(),
			project.getCostCenterCode(),
			project.getType().getName(),
			project.getSpecialty().getName(),
			project.getClient().getName(),
			project.getStatus().getName(),
			project.getStartDate(),
			project.getEndDate()
		);
	}

	public static ProjectDetailDto toDetailDto(Project project) {
		return new ProjectDetailDto(
			project.getId(),
			project.getName(),
			project.getCostCenterCode(),
			new RelationSummaryDto(project.getType().getId(), project.getType().getName()),
			new RelationSummaryDto(project.getSpecialty().getId(), project.getSpecialty().getName()),
			new RelationSummaryDto(project.getClient().getId(), project.getClient().getName()),
			new PlatformStatusOptionDto(project.getStatus().getId(), project.getStatus().getCode(), project.getStatus().getName()),
			project.getStartDate(),
			project.getEndDate(),
			project.getCreatedAt(),
			project.getUpdatedAt()
		);
	}
}
