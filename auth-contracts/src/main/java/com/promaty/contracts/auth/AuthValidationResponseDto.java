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

	private AuthOutcome outcome;
	private Long userId;
	private String role;
	private String fullName;
	private List<String> permissions;
	private Boolean allowedAllProjects;
	private List<Long> projectIds;
	private Boolean systemRole;
	private Long retryAfterSeconds;

	public static AuthValidationResponseDto rejected(AuthOutcome outcome) {
		return new AuthValidationResponseDto(outcome, null, null, null, null, null, null, null, null);
	}

	public static AuthValidationResponseDto locked(long retryAfterSeconds) {
		return new AuthValidationResponseDto(
			AuthOutcome.ACCOUNT_LOCKED, null, null, null, null, null, null, null, retryAfterSeconds);
	}
}
