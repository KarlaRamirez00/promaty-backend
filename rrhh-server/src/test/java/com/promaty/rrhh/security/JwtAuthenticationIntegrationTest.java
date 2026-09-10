package com.promaty.rrhh.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.controller.client.ClientController;
import com.promaty.rrhh.services.client.ClientService;
import com.promaty.rrhh.support.TestJwt;

// Ejercita el JwtAuthenticationFilter real de security-commons a traves de la cadena de seguridad.
@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class JwtAuthenticationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClientService clientService;

	@Test
	void endpointProtegido_sinToken_retorna401ConShapeDeContrato() throws Exception {
		mockMvc.perform(get("/clients"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.status").value(401))
			.andExpect(jsonPath("$.error.name").value("UNAUTHORIZED"))
			.andExpect(jsonPath("$.error.message").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void endpointProtegido_conTokenValido_pasaLaAutenticacion() throws Exception {
		when(clientService.listClients(any(), any())).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/clients").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("client.read")))
			.andExpect(status().isOk());
	}

	@Test
	void endpointProtegido_conTokenBasura_retorna401() throws Exception {
		mockMvc.perform(get("/clients").header(HttpHeaders.AUTHORIZATION, "Bearer esto-no-es-un-jwt"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.status").value(401));
	}
}
