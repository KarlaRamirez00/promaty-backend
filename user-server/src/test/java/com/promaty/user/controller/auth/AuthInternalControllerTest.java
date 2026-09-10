package com.promaty.user.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;
import com.promaty.user.config.SecurityConfig;
import com.promaty.user.services.auth.AuthService;
import com.promaty.user.support.TestJwt;

// /internal/** esta en la allowlist de SecurityConfig, por eso estas llamadas no llevan token.
@WebMvcTest(AuthInternalController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class AuthInternalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	@Test
	void validate_conCredencialesValidas_retorna200ConValidTrue() throws Exception {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		AuthValidationResponseDto respuesta = new AuthValidationResponseDto(true, 1L, "Editor", "Ana Pérez",
				List.of("warehouse.read"), false, List.of(10L));
		when(authService.validate(any())).thenReturn(respuesta);

		mockMvc.perform(post("/internal/auth/validate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.valid").value(true))
			.andExpect(jsonPath("$.data.role").value("Editor"))
			.andExpect(jsonPath("$.data.fullName").value("Ana Pérez"));
	}

	@Test
	void validate_conCredencialesInvalidas_retorna200ConValidFalse() throws Exception {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "claveIncorrecta");
		when(authService.validate(any())).thenReturn(AuthValidationResponseDto.invalid());

		mockMvc.perform(post("/internal/auth/validate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.valid").value(false));
	}

	@Test
	void validate_conEmailEnBlanco_retorna400() throws Exception {
		AuthValidationRequestDto dto = requestDto(" ", "clave123");

		mockMvc.perform(post("/internal/auth/validate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.email").exists());
	}

	private AuthValidationRequestDto requestDto(String email, String password) {
		AuthValidationRequestDto dto = new AuthValidationRequestDto();
		dto.setEmail(email);
		dto.setPassword(password);
		return dto;
	}
}
