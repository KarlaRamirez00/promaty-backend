package com.promaty.gateway.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.gateway.config.GatewayRoutesConfig;
import com.promaty.gateway.config.SecurityConfig;
import com.promaty.gateway.support.TestJwt;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest
@Import({SecurityConfig.class, GatewayRoutesConfig.class})
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class JwtAuthenticationIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rutaProtegida_sinToken_retorna401ConShapeDeContrato() throws Exception {
		mockMvc.perform(get("/roles"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.status").value(401))
			.andExpect(jsonPath("$.error.name").value("UNAUTHORIZED"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void rutaProtegida_conTokenBasura_retorna401() throws Exception {
		mockMvc.perform(get("/roles").header(HttpHeaders.AUTHORIZATION, "Bearer esto-no-es-un-jwt"))
			.andExpect(status().isUnauthorized());
	}
}
