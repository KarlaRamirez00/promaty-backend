package com.promaty.authorizer.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import com.promaty.authorizer.exception.AccountLockedException;
import com.promaty.authorizer.exception.InvalidCredentialsException;
import com.promaty.authorizer.exception.PasswordChangeRequiredException;
import com.promaty.authorizer.exception.TemporaryPasswordExpiredException;
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
	void login_conClaveTemporalVigente_retorna428ConNombreDeErrorPropio() throws Exception {
		LoginRequestDto dto = loginDto("ana@promaty.com", "ABCD2345");
		when(authService.login(any())).thenThrow(new PasswordChangeRequiredException("Debes cambiar tu contrasena temporal."));

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isPreconditionRequired())
			.andExpect(jsonPath("$.error.status").value(428))
			.andExpect(jsonPath("$.error.name").value("PASSWORD_CHANGE_REQUIRED"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void login_conClaveTemporalExpirada_retorna401ConNombreDeErrorPropio() throws Exception {
		LoginRequestDto dto = loginDto("ana@promaty.com", "ABCD2345");
		when(authService.login(any())).thenThrow(new TemporaryPasswordExpiredException("Tu contrasena temporal expiro."));

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error.status").value(401))
			.andExpect(jsonPath("$.error.name").value("TEMPORARY_PASSWORD_EXPIRED"));
	}

	@Test
	void login_conCuentaBloqueada_retorna429ConNombreCabeceraRetryAfterYTiempoEnElCuerpo() throws Exception {
		LoginRequestDto dto = loginDto("ana@promaty.com", "clave123");
		when(authService.login(any())).thenThrow(
			new AccountLockedException("Demasiados intentos fallidos. Intenta de nuevo en 15 minutos.", 900));

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isTooManyRequests())
			.andExpect(header().string("Retry-After", "900"))
			.andExpect(jsonPath("$.error.status").value(429))
			.andExpect(jsonPath("$.error.name").value("ACCOUNT_LOCKED"))
			.andExpect(jsonPath("$.meta.retryAfterSeconds").value(900))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void login_conEmailDeMasDe254Caracteres_retorna400ConErrorEnElCampoEmail() throws Exception {
		LoginRequestDto dto = loginDto("a".repeat(250) + "@b.cl", "clave123");

		mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.email").exists());
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
