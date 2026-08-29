package com.promaty.user.services.role.business.mapper;

import com.promaty.user.dto.permission.PermissionSummaryDto;
import com.promaty.user.entity.Permission;

public final class PermissionMapper {

	private PermissionMapper() {
	}

	public static PermissionSummaryDto toSummaryDto(Permission permission) {
		if (permission == null) {
			return null;
		}
		return new PermissionSummaryDto(permission.getId(), permission.getName());
	}
}
