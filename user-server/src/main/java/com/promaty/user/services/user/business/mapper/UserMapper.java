package com.promaty.user.services.user.business.mapper;

import com.promaty.user.dto.user.UserDetailDto;
import com.promaty.user.dto.user.UserListDto;
import com.promaty.user.entity.User;
import com.promaty.user.services.role.business.mapper.RoleMapper;

public final class UserMapper {

	private UserMapper() {
	}

	public static UserListDto toListDto(User user) {
		return new UserListDto(
			user.getId(),
			user.getFirstName(),
			user.getLastName(),
			user.getEmail(),
			user.getActive(),
			RoleMapper.toSummaryDto(user.getRole())
		);
	}

	public static UserDetailDto toDetailDto(User user) {
		return new UserDetailDto(
			user.getId(),
			user.getFirstName(),
			user.getLastName(),
			user.getEmail(),
			user.getPhoneNumber(),
			user.getActive(),
			RoleMapper.toSummaryDto(user.getRole()),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
