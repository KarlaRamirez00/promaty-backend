package com.promaty.user.services.permission;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.user.dto.permission.PermissionCatalogDto;
import com.promaty.user.repository.PermissionRepository;
import com.promaty.user.services.permission.business.mapper.PermissionMapper;

@Service
public class PermissionServiceImpl implements PermissionService {

	private final PermissionRepository permissionRepository;

	public PermissionServiceImpl(PermissionRepository permissionRepository) {
		this.permissionRepository = permissionRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<PermissionCatalogDto> listAllPermissions() {
		return permissionRepository.findAll(Sort.by("name"))
			.stream()
			.map(PermissionMapper::toCatalogDto)
			.toList();
	}
}
