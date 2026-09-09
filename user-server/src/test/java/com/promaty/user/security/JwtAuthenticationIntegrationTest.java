package com.promaty.user.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.contracts.auth.AuthValidationResponseDto;
import com.promaty.user.config.SecurityConfig;
import com.promaty.user.controller.auth.AuthInternalController;
import com.promaty.user.controller.role.RoleController;
import com.promaty.user.services.auth.AuthService;
import com.promaty.user.services.role.RoleService;
import com.promaty.user.support.TestJwt;

// Ejercita el JwtAuthenticationFilter real de security-commons a traves de la cadena de seguridad.
@WebMvcTest({RoleController.class, AuthInternalController.class})
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class JwtAuthenticationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RoleService roleService;

	@MockitoBean
	private AuthService authService;

	@Test
	void endpointProtegido_sinToken_retorna401ConShapeDeContrato() throws Exception {
		mockMvc.perform(get("/roles"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.status").value(401))
			.andExpect(jsonPath("$.error.name").value("UNAUTHORIZED"))
			.andExpect(jsonPath("$.error.message").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void endpointProtegido_conTokenValido_pasaLaAutenticacion() throws Exception {
		when(roleService.listRoles(any(), any())).thenReturn(new PageImpl<>(List.of()));

		// El token lleva role.read: GET /roles exige ese permiso via @PreAuthorize. Aca se prueba que
		// un token valido autentica y llega al controller; el detalle de 403/200 por permiso vive en
		// RoleControllerTest.
		mockMvc.perform(get("/roles").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("role.read")))
			.andExpect(status().isOk());
	}

	@Test
	void endpointProtegido_conTokenBasura_retorna401() throws Exception {
		mockMvc.perform(get("/roles").header(HttpHeaders.AUTHORIZATION, "Bearer esto-no-es-un-jwt"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.status").value(401));
	}

	@Test
	void rutaInterna_sinToken_noEstaProtegida() throws Exception {
		when(authService.validate(any())).thenReturn(new AuthValidationResponseDto(
			true, 1L, "Editor", List.of("rrhh.read"), false, List.of(10L)));

		mockMvc.perform(post("/internal/auth/validate")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"a@b.cl\",\"password\":\"x\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.valid").value(true));
	}
}
