package com.promaty.user.services.role.business.mapper;

import com.promaty.user.dto.submodule.SubModuleSummaryDto;
import com.promaty.user.entity.SubModule;

public final class SubModuleMapper {

	private SubModuleMapper() {
	}

	public static SubModuleSummaryDto toSummaryDto(SubModule subModule) {
		if (subModule == null) {
			return null;
		}
		return new SubModuleSummaryDto(subModule.getId(), subModule.getName());
	}
}
