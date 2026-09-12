package com.promaty.user.services.permission.business.mapper;

import com.promaty.user.dto.permission.PermissionCatalogDto;
import com.promaty.user.dto.submodule.SubModuleSummaryDto;
import com.promaty.user.entity.Permission;

public final class PermissionMapper {

	private PermissionMapper() {
	}

	public static PermissionCatalogDto toCatalogDto(Permission permission) {
		SubModuleSummaryDto subModulo = new SubModuleSummaryDto(
			permission.getSubModule().getId(),
			permission.getSubModule().getName(),
			permission.getSubModule().getAlias()
		);
		return new PermissionCatalogDto(
			permission.getId(),
			permission.getName(),
			permission.getAlias(),
			permission.getDescription(),
			subModulo
		);
	}
}
