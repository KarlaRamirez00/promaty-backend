package com.promaty.user.controller.role;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.user.dto.response.BaseData;
import com.promaty.user.dto.response.BaseListData;
import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.dto.role.RoleDetailDto;
import com.promaty.user.dto.role.RoleFilterParams;
import com.promaty.user.dto.role.RoleListDto;
import com.promaty.user.dto.role.RoleActiveUpdateDto;
import com.promaty.user.dto.role.RoleActiveUpdateResultDto;
import com.promaty.user.dto.role.UpdateRoleDto;
import com.promaty.user.services.role.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/roles")
public class RoleController {

	private final RoleService roleService;

	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('role.create')")
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateRoleDto dto) {
		Long id = roleService.createRole(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('role.read')")
	public ResponseEntity<BaseListData<RoleListDto>> list(
		@ModelAttribute RoleFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(roleService.listRoles(filters, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('role.read')")
	public ResponseEntity<BaseData<RoleDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(roleService.getRoleDetail(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('role.update')")
	public ResponseEntity<BaseData<RoleDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateRoleDto dto
	) {
		roleService.updateRole(id, dto);
		return ResponseEntity.ok(BaseData.success(roleService.getRoleDetail(id)));
	}

	@PatchMapping("/{id}/active")
	@PreAuthorize("hasAuthority('role.active')")
	public ResponseEntity<BaseData<RoleActiveUpdateResultDto>> toggleActive(
		@PathVariable Long id,
		@RequestBody(required = false) RoleActiveUpdateDto dto
	) {
		RoleActiveUpdateDto body = dto != null ? dto : new RoleActiveUpdateDto();
		return ResponseEntity.ok(BaseData.success(roleService.toggleRoleActive(id, body)));
	}
}
