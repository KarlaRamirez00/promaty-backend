package com.promaty.user.services.role.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.entity.Role;

@Component
public class CreateRoleBuilder {

	private final RoleRelationsResolver relationsResolver;

	public CreateRoleBuilder(RoleRelationsResolver relationsResolver) {
		this.relationsResolver = relationsResolver;
	}

	public Role build(CreateRoleDto dto) {
		Role role = new Role();
		role.setName(dto.getName());
		role.setDescription(dto.getDescription());
		role.setActive(true);
		role.setPermissions(relationsResolver.resolvePermissions(dto.getPermissionIds()));
		role.setSubModules(relationsResolver.resolveSubModules(dto.getSubModuleIds()));
		return role;
	}
}
