package com.promaty.user.services.role.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.SubModule;

@ExtendWith(MockitoExtension.class)
class CreateRoleBuilderTest {

	@Mock
	private RoleRelationsResolver relationsResolver;

	@InjectMocks
	private CreateRoleBuilder createRoleBuilder;

	@Test
	void build_conDatosValidos_armaRoleConEstadoActivoYRelacionesResueltas() {
		CreateRoleDto dto = new CreateRoleDto();
		dto.setName("Editor");
		dto.setDescription("Rol editor");
		dto.setPermissionIds(List.of(1L));
		dto.setSubModuleIds(List.of(2L));

		Set<Permission> permisos = Set.of(new Permission());
		Set<SubModule> subModulos = Set.of(new SubModule());
		when(relationsResolver.resolvePermissions(List.of(1L))).thenReturn(permisos);
		when(relationsResolver.resolveSubModules(List.of(2L))).thenReturn(subModulos);

		Role role = createRoleBuilder.build(dto);

		assertThat(role.getName()).isEqualTo("Editor");
		assertThat(role.getDescription()).isEqualTo("Rol editor");
		assertThat(role.getActive()).isTrue();
		assertThat(role.getPermissions()).isEqualTo(permisos);
		assertThat(role.getSubModules()).isEqualTo(subModulos);
	}
}
