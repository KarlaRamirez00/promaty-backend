package com.promaty.user.dto.role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleActiveUpdateDto {

	// Solo obligatorio si el rol tiene usuarios asignados y se esta desactivando.
	private Long newRoleId;
}
