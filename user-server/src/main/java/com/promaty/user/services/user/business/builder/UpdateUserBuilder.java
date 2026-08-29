package com.promaty.user.services.user.business.builder;

import com.promaty.user.dto.user.UpdateUserDto;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;

public final class UpdateUserBuilder {

	private UpdateUserBuilder() {
	}

	public static User apply(User existente, UpdateUserDto dto, Role role) {
		existente.setFirstName(dto.getFirstName());
		existente.setLastName(dto.getLastName());
		existente.setEmail(dto.getEmail());
		existente.setPhoneNumber(dto.getPhoneNumber());
		existente.setRole(role);
		return existente;
	}
}
