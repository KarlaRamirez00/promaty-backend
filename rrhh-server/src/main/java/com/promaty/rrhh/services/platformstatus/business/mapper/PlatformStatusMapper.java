package com.promaty.rrhh.services.platformstatus.business.mapper;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.entity.PlatformStatus;

public final class PlatformStatusMapper {

	private PlatformStatusMapper() {
	}

	public static PlatformStatusOptionDto toOptionDto(PlatformStatus platformStatus) {
		return new PlatformStatusOptionDto(
			platformStatus.getId(),
			platformStatus.getCode(),
			platformStatus.getName()
		);
	}
}
