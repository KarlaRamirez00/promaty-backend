package com.promaty.user.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.contracts.auth.AuthOutcome;
import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;
import com.promaty.contracts.auth.InternalApiHeader;
import com.promaty.user.config.SecurityConfig;
import com.promaty.user.services.auth.AuthService;
import com.promaty.user.support.TestInternalApi;
import com.promaty.user.support.TestJwt;

@WebMvcTest(AuthInternalController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = { "jwt.secret=" + TestJwt.SECRET, "internal-api.secret=" + TestInternalApi.SECRET })
class AuthInternalControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	@Test
	void validate_conCredencialesValidas_retorna200ConOutcomeAuthenticated() throws Exception {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		AuthValidationResponseDto respuesta = new AuthValidationResponseDto(AuthOutcome.AUTHENTICATED, 1L, "Editor", "Ana Pérez",
				List.of("warehouse.read"), false, List.of(10L), false, null);
		when(authService.validate(any())).thenReturn(respuesta);

		mockMvc.perform(post("/internal/auth/validate")
				.header(InternalApiHeader.SECRET, TestInternalApi.SECRET)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.outcome").value("AUTHENTICATED"))
			.andExpect(jsonPath("$.data.role").value("Editor"))
			.andExpect(jsonPath("$.data.fullName").value("Ana Pérez"));
	}

	@Test
	void validate_conCredencialesInvalidas_retorna200ConOutcomeInvalidCredentials() throws Exception {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "claveIncorrecta");
		when(authService.validate(any())).thenReturn(AuthValidationResponseDto.rejected(AuthOutcome.INVALID_CREDENTIALS));

		mockMvc.perform(post("/internal/auth/validate")
				.header(InternalApiHeader.SECRET, TestInternalApi.SECRET)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.outcome").value("INVALID_CREDENTIALS"));
	}

	@Test
	void validate_conEmailEnBlanco_retorna400() throws Exception {
		AuthValidationRequestDto dto = requestDto(" ", "clave123");

		mockMvc.perform(post("/internal/auth/validate")
				.header(InternalApiHeader.SECRET, TestInternalApi.SECRET)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.email").exists());
	}

	@Test
	void validate_sinSecretoDeServicio_retorna401YNoLlegaAlService() throws Exception {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");

		mockMvc.perform(post("/internal/auth/validate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.name").value("UNAUTHORIZED"));

		verifyNoInteractions(authService);
	}

	@Test
	void validate_conSecretoIncorrecto_retorna401() throws Exception {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");

		mockMvc.perform(post("/internal/auth/validate")
				.header(InternalApiHeader.SECRET, "otro-secreto-distinto-0123456789-abcdef")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isUnauthorized());

		verifyNoInteractions(authService);
	}

	@Test
	void validate_conTokenDeUsuarioPeroSinSecreto_retorna403() throws Exception {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");

		mockMvc.perform(post("/internal/auth/validate")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("user.read"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isForbidden());

		verifyNoInteractions(authService);
	}

	private AuthValidationRequestDto requestDto(String email, String password) {
		AuthValidationRequestDto dto = new AuthValidationRequestDto();
		dto.setEmail(email);
		dto.setPassword(password);
		return dto;
	}
}
