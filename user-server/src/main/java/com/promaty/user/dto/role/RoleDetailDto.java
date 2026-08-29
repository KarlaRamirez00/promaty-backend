package com.promaty.user.dto.role;

import java.util.List;

import com.promaty.user.dto.permission.PermissionSummaryDto;
import com.promaty.user.dto.submodule.SubModuleSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailDto {

	private Long id;
	private String name;
	private String description;
	private Boolean active;
	private Long totalUsers;
	private List<PermissionSummaryDto> permissions;
	private List<SubModuleSummaryDto> subModules;
}
