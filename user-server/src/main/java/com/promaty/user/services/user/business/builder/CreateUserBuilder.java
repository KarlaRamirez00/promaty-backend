package com.promaty.user.services.user.business.builder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.promaty.user.dto.user.CreateUserDto;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.repository.RoleRepository;

@Component
public class CreateUserBuilder {

	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public CreateUserBuilder(RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public User build(CreateUserDto dto) {
		// getReferenceById: UserValidation ya confirmo que el rol existe, evita un SELECT extra.
		Role role = roleRepository.getReferenceById(dto.getRoleId());

		User user = new User();
		user.setFirstName(dto.getFirstName());
		user.setLastName(dto.getLastName());
		user.setEmail(dto.getEmail());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setPhoneNumber(dto.getPhoneNumber());
		user.setActive(true);
		user.setRole(role);
		return user;
	}
}
