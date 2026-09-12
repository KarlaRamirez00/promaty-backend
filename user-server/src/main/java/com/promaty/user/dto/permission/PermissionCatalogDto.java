package com.promaty.user.dto.permission;

import com.promaty.user.dto.submodule.SubModuleSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionCatalogDto {

	private Long id;
	private String name;
	private String alias;
	private String description;
	private SubModuleSummaryDto subModule;
}
