package com.promaty.contracts.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthValidationResponseDto {

	private Boolean valid;
	private Long userId;
	private String role;
	private List<String> permissions;
	private Boolean allowedAllProjects;
	private List<Long> projectIds;

	public static AuthValidationResponseDto invalid() {
		return new AuthValidationResponseDto(false, null, null, null, null, null);
	}
}
