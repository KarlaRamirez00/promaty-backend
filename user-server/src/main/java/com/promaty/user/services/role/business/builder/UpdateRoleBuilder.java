package com.promaty.user.services.role.business.builder;

import java.util.Set;

import com.promaty.user.dto.role.UpdateRoleDto;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.SubModule;

public final class UpdateRoleBuilder {

	private UpdateRoleBuilder() {
	}

	public static Role apply(Role existente, UpdateRoleDto dto, Set<Permission> permisos, Set<SubModule> subModulos) {
		existente.setName(dto.getName());
		existente.setDescription(dto.getDescription());
		existente.setPermissions(permisos);
		existente.setSubModules(subModulos);
		return existente;
	}
}
