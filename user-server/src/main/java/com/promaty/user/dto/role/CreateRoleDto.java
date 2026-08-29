package com.promaty.user.dto.role;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleDto {

	@NotBlank(message = "El nombre es obligatorio.")
	private String name;

	private String description;

	private List<Long> permissionIds;

	private List<Long> subModuleIds;
}
