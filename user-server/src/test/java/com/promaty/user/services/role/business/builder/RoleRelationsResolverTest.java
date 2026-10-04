package com.promaty.user.services.role.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.user.entity.Permission;
import com.promaty.user.entity.SubModule;
import com.promaty.user.repository.PermissionRepository;

@ExtendWith(MockitoExtension.class)
class RoleRelationsResolverTest {

	@Mock
	private PermissionRepository permissionRepository;

	@InjectMocks
	private RoleRelationsResolver relationsResolver;

	@Test
	void resolvePermissions_conIdsNulos_retornaSetVacioSinConsultarRepo() {
		Set<Permission> resultado = relationsResolver.resolvePermissions(null);

		assertThat(resultado).isEmpty();
		verifyNoInteractions(permissionRepository);
	}

	@Test
	void resolvePermissions_conIdsVacios_retornaSetVacioSinConsultarRepo() {
		Set<Permission> resultado = relationsResolver.resolvePermissions(List.of());

		assertThat(resultado).isEmpty();
		verifyNoInteractions(permissionRepository);
	}

	@Test
	void resolvePermissions_conIds_retornaLosPermisosDelRepo() {
		Permission permiso = new Permission();
		when(permissionRepository.findByIdIn(List.of(1L))).thenReturn(List.of(permiso));

		Set<Permission> resultado = relationsResolver.resolvePermissions(List.of(1L));

		assertThat(resultado).containsExactly(permiso);
	}

	@Test
	void deriveSubModules_conPermisosDeDistintosSubmodulos_retornaSubModulosSinDuplicar() {
		SubModule staffSubModule = new SubModule();
		staffSubModule.setId(10L);
		Permission staffRead = new Permission();
		staffRead.setSubModule(staffSubModule);
		Permission staffCreate = new Permission();
		staffCreate.setSubModule(staffSubModule);

		Set<SubModule> resultado = relationsResolver.deriveSubModules(Set.of(staffRead, staffCreate));

		assertThat(resultado).containsExactly(staffSubModule);
	}

	@Test
	void deriveSubModules_sinPermisos_retornaSetVacio() {
		Set<SubModule> resultado = relationsResolver.deriveSubModules(Set.of());

		assertThat(resultado).isEmpty();
	}
}
