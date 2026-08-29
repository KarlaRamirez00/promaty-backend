package com.promaty.user.services.auth;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.User;
import com.promaty.user.repository.UserProjectAccessRepository;
import com.promaty.user.repository.UserRepository;

@Service
public class AuthServiceImpl implements AuthService {

	private static final String ALLOWED_ALL_SUFFIX = ".AllowedAll";

	private final UserRepository userRepository;
	private final UserProjectAccessRepository userProjectAccessRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthServiceImpl(UserRepository userRepository, UserProjectAccessRepository userProjectAccessRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.userProjectAccessRepository = userProjectAccessRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional(readOnly = true)
	public AuthValidationResponseDto validate(AuthValidationRequestDto dto) {
		return userRepository.findByEmail(dto.getEmail())
			.filter(user -> passwordEncoder.matches(dto.getPassword(), user.getPassword()))
			.filter(User::getActive)
			.map(this::buildValidResponse)
			.orElseGet(AuthValidationResponseDto::invalid);
	}

	private AuthValidationResponseDto buildValidResponse(User user) {
		List<String> permissions = user.getRole().getPermissions().stream()
			.map(Permission::getName)
			.toList();
		boolean allowedAllProjects = permissions.stream().anyMatch(name -> name.endsWith(ALLOWED_ALL_SUFFIX));
		List<Long> projectIds = allowedAllProjects
			? List.of()
			: userProjectAccessRepository.findProjectIdsByUserId(user.getId());
		return new AuthValidationResponseDto(true, user.getId(), user.getRole().getName(), permissions,
			allowedAllProjects, projectIds);
	}
}
