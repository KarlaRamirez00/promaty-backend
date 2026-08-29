package com.promaty.user.services.role.business.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.promaty.user.entity.Permission;
import com.promaty.user.entity.SubModule;
import com.promaty.user.repository.PermissionRepository;
import com.promaty.user.repository.SubModuleRepository;

@Component
public class RoleRelationsResolver {

	private final PermissionRepository permissionRepository;
	private final SubModuleRepository subModuleRepository;

	public RoleRelationsResolver(PermissionRepository permissionRepository, SubModuleRepository subModuleRepository) {
		this.permissionRepository = permissionRepository;
		this.subModuleRepository = subModuleRepository;
	}

	public Set<Permission> resolvePermissions(List<Long> permissionIds) {
		if (permissionIds == null || permissionIds.isEmpty()) {
			return new HashSet<>();
		}
		return new HashSet<>(permissionRepository.findByIdIn(permissionIds));
	}

	public Set<SubModule> resolveSubModules(List<Long> subModuleIds) {
		if (subModuleIds == null || subModuleIds.isEmpty()) {
			return new HashSet<>();
		}
		return new HashSet<>(subModuleRepository.findByIdIn(subModuleIds));
	}
}
