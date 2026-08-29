package com.promaty.authorizer.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.authorizer.config.SecurityConfig;
import com.promaty.authorizer.dto.auth.LoginRequestDto;
import com.promaty.authorizer.dto.auth.LoginResponseDto;
import com.promaty.authorizer.exception.InvalidCredentialsException;
import com.promaty.authorizer.services.auth.AuthService;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	@Test
	void login_conCredencialesValidas_retorna200ConToken() throws Exception {
		LoginRequestDto dto = loginDto("ana@promaty.com", "clave123");
		when(authService.login(any())).thenReturn(new LoginResponseDto("token.jwt.simulado"));

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.token").value("token.jwt.simulado"));
	}

	@Test
	void login_conCredencialesInvalidas_retorna401() throws Exception {
		LoginRequestDto dto = loginDto("ana@promaty.com", "claveIncorrecta");
		when(authService.login(any())).thenThrow(new InvalidCredentialsException("Email o contrasena incorrectos."));

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.message").value("Email o contrasena incorrectos."));
	}

	@Test
	void login_conEmailEnBlanco_retorna400() throws Exception {
		LoginRequestDto dto = loginDto(" ", "clave123");

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.email").exists());
	}

	private LoginRequestDto loginDto(String email, String password) {
		LoginRequestDto dto = new LoginRequestDto();
		dto.setEmail(email);
		dto.setPassword(password);
		return dto;
	}
}
