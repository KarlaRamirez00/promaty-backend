package com.promaty.user.dto.user;

import com.promaty.user.dto.role.RoleSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserListDto {

	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private Boolean status;
	private RoleSummaryDto role;
}
