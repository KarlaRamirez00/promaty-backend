package com.promaty.user.controller.permission;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.user.config.SecurityConfig;
import com.promaty.user.dto.permission.PermissionCatalogDto;
import com.promaty.user.dto.submodule.SubModuleSummaryDto;
import com.promaty.user.services.permission.PermissionService;
import com.promaty.user.support.TestJwt;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(PermissionController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class PermissionControllerTest {

	// Token con role.read: es el mismo permiso que ya exige el editor de rol para ver el catálogo.
	private static final String TOKEN = TestJwt.bearer("role.read");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PermissionService permissionService;

	@Test
	void list_conPermiso_retorna200ConElCatalogo() throws Exception {
		when(permissionService.listAllPermissions()).thenReturn(List.of(
			new PermissionCatalogDto(1L, "client.read", "Ver mandantes", null,
				new SubModuleSummaryDto(3L, "clients", "Mandantes"))
		));

		mockMvc.perform(get("/permissions").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("client.read"))
			.andExpect(jsonPath("$.data[0].subModule.alias").value("Mandantes"))
			.andExpect(jsonPath("$.meta.pagination").doesNotExist());
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/permissions").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}
}
