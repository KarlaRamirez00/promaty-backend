package com.promaty.user.dto.user;

import java.time.LocalDateTime;

import com.promaty.user.dto.role.RoleSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDto {

	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private Boolean active;
	private RoleSummaryDto role;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
