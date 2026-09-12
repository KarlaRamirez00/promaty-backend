package com.promaty.user.services.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.promaty.user.dto.permission.PermissionCatalogDto;
import com.promaty.user.entity.Module;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.SubModule;
import com.promaty.user.repository.PermissionRepository;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

	@Mock
	private PermissionRepository permissionRepository;

	@InjectMocks
	private PermissionServiceImpl service;

	@Test
	void listAllPermissions_devuelveElCatalogoConSuSubModulo() {
		when(permissionRepository.findAll(any(Sort.class)))
			.thenReturn(List.of(permiso("client.read", "Ver mandantes", "clients", "Mandantes")));

		List<PermissionCatalogDto> catalogo = service.listAllPermissions();

		assertThat(catalogo).hasSize(1);
		assertThat(catalogo.get(0).getName()).isEqualTo("client.read");
		assertThat(catalogo.get(0).getSubModule().getAlias()).isEqualTo("Mandantes");
	}

	private Permission permiso(String name, String alias, String subModuleName, String subModuleAlias) {
		Module modulo = new Module();
		modulo.setId(1L);
		modulo.setName("sistema");
		modulo.setAlias("Sistema");

		SubModule subModulo = new SubModule();
		subModulo.setId(3L);
		subModulo.setName(subModuleName);
		subModulo.setAlias(subModuleAlias);
		subModulo.setPath("/x");
		subModulo.setModule(modulo);

		Permission permiso = new Permission();
		permiso.setId(9L);
		permiso.setName(name);
		permiso.setAlias(alias);
		permiso.setSubModule(subModulo);
		return permiso;
	}
}
