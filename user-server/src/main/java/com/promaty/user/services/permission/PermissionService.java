package com.promaty.user.services.permission;

import java.util.List;

import com.promaty.user.dto.permission.PermissionCatalogDto;

public interface PermissionService {

	List<PermissionCatalogDto> listAllPermissions();
}
