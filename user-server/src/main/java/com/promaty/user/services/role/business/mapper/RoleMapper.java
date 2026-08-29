package com.promaty.user.services.role.business.mapper;

import java.util.List;

import com.promaty.user.dto.permission.PermissionSummaryDto;
import com.promaty.user.dto.role.RoleDetailDto;
import com.promaty.user.dto.role.RoleListDto;
import com.promaty.user.dto.role.RoleSummaryDto;
import com.promaty.user.dto.submodule.SubModuleSummaryDto;
import com.promaty.user.entity.Role;

public final class RoleMapper {

	private RoleMapper() {
	}

	public static RoleSummaryDto toSummaryDto(Role role) {
		return new RoleSummaryDto(role.getId(), role.getName());
	}

	public static RoleListDto toListDto(Role role, Long totalUsers) {
		return new RoleListDto(role.getId(), role.getName(), role.getDescription(), role.getActive(), totalUsers);
	}

	public static RoleDetailDto toDetailDto(Role role, Long totalUsers) {
		List<PermissionSummaryDto> permisos = role.getPermissions().stream()
			.map(PermissionMapper::toSummaryDto)
			.toList();
		List<SubModuleSummaryDto> subModulos = role.getSubModules().stream()
			.map(SubModuleMapper::toSummaryDto)
			.toList();

		return new RoleDetailDto(
			role.getId(),
			role.getName(),
			role.getDescription(),
			role.getActive(),
			totalUsers,
			permisos,
			subModulos
		);
	}
}
