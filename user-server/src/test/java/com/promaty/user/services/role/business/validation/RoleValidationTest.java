package com.promaty.user.services.role.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.dto.role.UpdateRoleDto;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.SubModule;
import com.promaty.user.exception.BusinessValidationException;
import com.promaty.user.repository.PermissionRepository;
import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.SubModuleRepository;

@ExtendWith(MockitoExtension.class)
class RoleValidationTest {

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PermissionRepository permissionRepository;

	@Mock
	private SubModuleRepository subModuleRepository;

	@InjectMocks
	private RoleValidation roleValidation;

	@Test
	void validateCreate_conNombreDuplicado_lanzaErrorEnName() {
		CreateRoleDto dto = createDto("Administrador", null, null);
		when(roleRepository.existsByName("Administrador")).thenReturn(true);

		assertThatThrownBy(() -> roleValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateCreate_conPermisoInexistente_lanzaErrorEnPermissionIds() {
		CreateRoleDto dto = createDto("Editor", List.of(1L, 2L), null);
		when(roleRepository.existsByName("Editor")).thenReturn(false);
		when(permissionRepository.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(new Permission()));

		assertThatThrownBy(() -> roleValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("permissionIds"));
	}

	@Test
	void validateCreate_conSubModuloInexistente_lanzaErrorEnSubModuleIds() {
		CreateRoleDto dto = createDto("Editor", null, List.of(1L, 2L));
		when(roleRepository.existsByName("Editor")).thenReturn(false);
		when(subModuleRepository.findByIdIn(List.of(1L, 2L))).thenReturn(List.of(new SubModule()));

		assertThatThrownBy(() -> roleValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("subModuleIds"));
	}

	@Test
	void validateCreate_conDatosValidos_noLanzaExcepcion() {
		CreateRoleDto dto = createDto("Editor", List.of(1L), List.of(2L));
		when(roleRepository.existsByName("Editor")).thenReturn(false);
		when(permissionRepository.findByIdIn(List.of(1L))).thenReturn(List.of(new Permission()));
		when(subModuleRepository.findByIdIn(List.of(2L))).thenReturn(List.of(new SubModule()));

		assertThatCode(() -> roleValidation.validateCreate(dto)).doesNotThrowAnyException();
	}

	@Test
	void validateCreate_conVariosErrores_losAcumulaTodos() {
		CreateRoleDto dto = createDto("Administrador", List.of(1L), List.of(2L));
		when(roleRepository.existsByName("Administrador")).thenReturn(true);
		when(permissionRepository.findByIdIn(anyList())).thenReturn(List.of());
		when(subModuleRepository.findByIdIn(anyList())).thenReturn(List.of());

		assertThatThrownBy(() -> roleValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKeys("name", "permissionIds", "subModuleIds"));
	}

	@Test
	void validateUpdate_conNombreDuplicadoDeOtroRol_lanzaErrorEnName() {
		UpdateRoleDto dto = updateDto("Administrador", null, null);
		Role otroRol = new Role();
		otroRol.setId(99L);
		when(roleRepository.findByName("Administrador")).thenReturn(Optional.of(otroRol));

		assertThatThrownBy(() -> roleValidation.validateUpdate(1L, dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateUpdate_conNombreDuplicadoDelMismoRol_noLanzaExcepcion() {
		UpdateRoleDto dto = updateDto("Administrador", null, null);
		Role mismoRol = new Role();
		mismoRol.setId(1L);
		when(roleRepository.findByName("Administrador")).thenReturn(Optional.of(mismoRol));

		assertThatCode(() -> roleValidation.validateUpdate(1L, dto)).doesNotThrowAnyException();
	}

	private CreateRoleDto createDto(String name, List<Long> permissionIds, List<Long> subModuleIds) {
		CreateRoleDto dto = new CreateRoleDto();
		dto.setName(name);
		dto.setPermissionIds(permissionIds);
		dto.setSubModuleIds(subModuleIds);
		return dto;
	}

	private UpdateRoleDto updateDto(String name, List<Long> permissionIds, List<Long> subModuleIds) {
		UpdateRoleDto dto = new UpdateRoleDto();
		dto.setName(name);
		dto.setPermissionIds(permissionIds);
		dto.setSubModuleIds(subModuleIds);
		return dto;
	}
}
