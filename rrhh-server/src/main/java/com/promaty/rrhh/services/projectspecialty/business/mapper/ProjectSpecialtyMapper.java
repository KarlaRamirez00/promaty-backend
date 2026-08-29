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
			projectSpecialty.getActive()
		);
	}

	public static ProjectSpecialtyDetailDto toDetailDto(ProjectSpecialty projectSpecialty) {
		return new ProjectSpecialtyDetailDto(
			projectSpecialty.getId(),
			projectSpecialty.getName(),
			projectSpecialty.getActive(),
			projectSpecialty.getCreatedAt(),
			projectSpecialty.getUpdatedAt()
		);
	}
}
