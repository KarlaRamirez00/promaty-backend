package com.promaty.user.services.role.business.validation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.dto.role.UpdateRoleDto;
import com.promaty.user.entity.Role;
import com.promaty.user.exception.BusinessValidationException;
import com.promaty.user.repository.PermissionRepository;
import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.SubModuleRepository;

@Component
public class RoleValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";

	private final RoleRepository roleRepository;
	private final PermissionRepository permissionRepository;
	private final SubModuleRepository subModuleRepository;

	public RoleValidation(
		RoleRepository roleRepository,
		PermissionRepository permissionRepository,
		SubModuleRepository subModuleRepository
	) {
		this.roleRepository = roleRepository;
		this.permissionRepository = permissionRepository;
		this.subModuleRepository = subModuleRepository;
	}

	public void validateCreate(CreateRoleDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (roleRepository.existsByName(dto.getName())) {
			errores.put("name", "Ya existe un rol con este nombre.");
		}
		validarRelaciones(dto.getPermissionIds(), dto.getSubModuleIds(), errores);

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(Long id, UpdateRoleDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		Optional<Role> rolConMismoNombre = roleRepository.findByName(dto.getName());
		if (rolConMismoNombre.isPresent() && !rolConMismoNombre.get().getId().equals(id)) {
			errores.put("name", "Ya existe un rol con este nombre.");
		}
		validarRelaciones(dto.getPermissionIds(), dto.getSubModuleIds(), errores);

		lanzarSiHayErrores(errores);
	}

	private void validarRelaciones(List<Long> permissionIds, List<Long> subModuleIds, Map<String, String> errores) {
		if (permissionIds != null && !permissionIds.isEmpty()
			&& permissionRepository.findByIdIn(permissionIds).size() != permissionIds.size()) {
			errores.put("permissionIds", "Uno o mas permisos indicados no existen.");
		}
		if (subModuleIds != null && !subModuleIds.isEmpty()
			&& subModuleRepository.findByIdIn(subModuleIds).size() != subModuleIds.size()) {
			errores.put("subModuleIds", "Uno o mas submodulos indicados no existen.");
		}
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
