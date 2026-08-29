package com.promaty.user.dto.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleActiveUpdateResultDto {

	private Long id;
	private Boolean active;
	private Long reassignedUsers;
}
