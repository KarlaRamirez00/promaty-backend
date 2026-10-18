package com.promaty.user.services.user.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.user.dto.user.CreateUserDto;
import com.promaty.user.dto.user.UpdateUserDto;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.exception.BusinessValidationException;
import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.UserRepository;
import com.promaty.user.services.shared.CurrentUserAuthorities;

@Component
public class UserValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";
	private static final String PERMISO_ASIGNAR_ROL_SISTEMA = "user.assignSystemRole";

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
		validarRol(dto.getRoleId(), errores);

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(Long id, UpdateUserDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		validarRol(dto.getRoleId(), errores);

		Optional<User> usuarioConMismoEmail = userRepository.findByEmail(dto.getEmail());
		if (usuarioConMismoEmail.isPresent() && !usuarioConMismoEmail.get().getId().equals(id)) {
			errores.put("email", "Ya existe un usuario con este email.");
		}

		lanzarSiHayErrores(errores);
	}

	private void validarRol(Long roleId, Map<String, String> errores) {
		Optional<Role> rol = roleRepository.findById(roleId);
		if (rol.isEmpty()) {
			errores.put("roleId", "El rol indicado no existe.");
			return;
		}
		if (Boolean.TRUE.equals(rol.get().getSystem())
				&& !CurrentUserAuthorities.get().contains(PERMISO_ASIGNAR_ROL_SISTEMA)) {
			errores.put("roleId", "No tienes permiso para asignar un rol de sistema.");
		}
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
