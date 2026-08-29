package com.promaty.user.dto.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleListDto {

	private Long id;
	private String name;
	private String description;
	private Boolean active;
	private Long totalUsers;
}
