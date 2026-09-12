package com.promaty.user.controller.permission;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.user.dto.permission.PermissionCatalogDto;
import com.promaty.user.dto.response.BaseListData;
import com.promaty.user.services.permission.PermissionService;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

	private final PermissionService permissionService;

	public PermissionController(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('role.read')")
	public ResponseEntity<BaseListData<PermissionCatalogDto>> list() {
		return ResponseEntity.ok(BaseListData.of(permissionService.listAllPermissions()));
	}
}
