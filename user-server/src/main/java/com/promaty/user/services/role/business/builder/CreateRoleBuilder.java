package com.promaty.user.services.role.business.builder;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.SubModule;

@Component
public class CreateRoleBuilder {

	private final RoleRelationsResolver relationsResolver;

	public CreateRoleBuilder(RoleRelationsResolver relationsResolver) {
		this.relationsResolver = relationsResolver;
	}

	public Role build(CreateRoleDto dto) {
		Set<Permission> permisos = relationsResolver.resolvePermissions(dto.getPermissionIds());
		Role role = new Role();
		role.setName(dto.getName());
		role.setDescription(dto.getDescription());
		role.setActive(true);
		role.setPermissions(permisos);
		role.setSubModules(relationsResolver.deriveSubModules(permisos));
		return role;
	}
}
