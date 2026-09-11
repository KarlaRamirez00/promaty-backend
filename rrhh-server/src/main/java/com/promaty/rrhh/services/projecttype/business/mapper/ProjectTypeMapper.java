package com.promaty.rrhh.services.projecttype.business.mapper;

import com.promaty.rrhh.dto.projecttype.ProjectTypeDetailDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeListDto;
import com.promaty.rrhh.entity.ProjectType;

public final class ProjectTypeMapper {

	private ProjectTypeMapper() {
	}

	public static ProjectTypeListDto toListDto(ProjectType projectType) {
		return new ProjectTypeListDto(
			projectType.getId(),
			projectType.getName(),
			projectType.getActive(),
			projectType.getCreatedAt(),
			projectType.getUpdatedAt(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	public static ProjectTypeDetailDto toDetailDto(ProjectType projectType) {
		return new ProjectTypeDetailDto(
			projectType.getId(),
			projectType.getName(),
			projectType.getActive(),
			projectType.getCreatedAt(),
			projectType.getUpdatedAt(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}
}
