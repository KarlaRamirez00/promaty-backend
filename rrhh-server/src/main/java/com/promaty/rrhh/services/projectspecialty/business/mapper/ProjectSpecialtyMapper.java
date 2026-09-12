package com.promaty.rrhh.services.projectspecialty.business.mapper;

import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyDetailDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyListDto;
import com.promaty.rrhh.entity.ProjectSpecialty;

public final class ProjectSpecialtyMapper {

	private ProjectSpecialtyMapper() {
	}

	public static ProjectSpecialtyListDto toListDto(ProjectSpecialty projectSpecialty) {
		return new ProjectSpecialtyListDto(
			projectSpecialty.getId(),
			projectSpecialty.getName(),
			projectSpecialty.getActive(),
			projectSpecialty.getCreatedAt(),
			projectSpecialty.getUpdatedAt(),
			projectSpecialty.getCreatedBy(),
			projectSpecialty.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}

	public static ProjectSpecialtyDetailDto toDetailDto(ProjectSpecialty projectSpecialty) {
		return new ProjectSpecialtyDetailDto(
			projectSpecialty.getId(),
			projectSpecialty.getName(),
			projectSpecialty.getActive(),
			projectSpecialty.getCreatedAt(),
			projectSpecialty.getUpdatedAt(),
			projectSpecialty.getCreatedBy(),
			projectSpecialty.getUpdatedBy(),
			null // actions depende de los permisos del usuario que pide, no de la entidad: lo completa el service
		);
	}
}
