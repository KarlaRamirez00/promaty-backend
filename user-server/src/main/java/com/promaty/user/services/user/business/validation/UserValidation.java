package com.promaty.user.services.user.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.user.dto.user.CreateUserDto;
import com.promaty.user.dto.user.UpdateUserDto;
import com.promaty.user.entity.User;
import com.promaty.user.exception.BusinessValidationException;
import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.UserRepository;

@Component
public class UserValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;

	public UserValidation(UserRepository userRepository, RoleRepository roleRepository) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
	}

	public void validateCreate(CreateUserDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (userRepository.existsByEmail(dto.getEmail())) {
			errores.put("email", "Ya existe un usuario con este email.");
		}
		if (!roleRepository.existsById(dto.getRoleId())) {
			errores.put("roleId", "El rol indicado no existe.");
		}

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(Long id, UpdateUserDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (!roleRepository.existsById(dto.getRoleId())) {
			errores.put("roleId", "El rol indicado no existe.");
		}

		Optional<User> usuarioConMismoEmail = userRepository.findByEmail(dto.getEmail());
		if (usuarioConMismoEmail.isPresent() && !usuarioConMismoEmail.get().getId().equals(id)) {
			errores.put("email", "Ya existe un usuario con este email.");
		}

		lanzarSiHayErrores(errores);
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
