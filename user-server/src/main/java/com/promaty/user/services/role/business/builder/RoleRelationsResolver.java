package com.promaty.user.services.role.business.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.promaty.user.entity.Permission;
import com.promaty.user.entity.SubModule;
import com.promaty.user.repository.PermissionRepository;

@Component
public class RoleRelationsResolver {

	private final PermissionRepository permissionRepository;

	public RoleRelationsResolver(PermissionRepository permissionRepository) {
		this.permissionRepository = permissionRepository;
	}

	public Set<Permission> resolvePermissions(List<Long> permissionIds) {
		if (permissionIds == null || permissionIds.isEmpty()) {
			return new HashSet<>();
		}
		return new HashSet<>(permissionRepository.findByIdIn(permissionIds));
	}

	public Set<SubModule> deriveSubModules(Set<Permission> permissions) {
		return permissions.stream()
			.map(Permission::getSubModule)
			.collect(Collectors.toSet());
	}
}
