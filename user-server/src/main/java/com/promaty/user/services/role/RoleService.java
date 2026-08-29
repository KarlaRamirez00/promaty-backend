package com.promaty.user.services.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.dto.role.RoleDetailDto;
import com.promaty.user.dto.role.RoleFilterParams;
import com.promaty.user.dto.role.RoleListDto;
import com.promaty.user.dto.role.RoleActiveUpdateDto;
import com.promaty.user.dto.role.RoleActiveUpdateResultDto;
import com.promaty.user.dto.role.UpdateRoleDto;

public interface RoleService {

	Long createRole(CreateRoleDto dto);

	void updateRole(Long id, UpdateRoleDto dto);

	Page<RoleListDto> listRoles(RoleFilterParams filters, Pageable pageable);

	RoleDetailDto getRoleDetail(Long id);

	RoleActiveUpdateResultDto toggleRoleActive(Long id, RoleActiveUpdateDto dto);
}
