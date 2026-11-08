package com.promaty.user.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.promaty.user.config.SecurityConfig;
import com.promaty.user.dto.auth.PasswordResetDto;
import com.promaty.user.exception.InvalidResetCodeException;
import com.promaty.user.services.auth.PasswordRecoveryService;
import com.promaty.user.services.auth.PasswordResetService;
import com.promaty.user.support.TestJwt;

// /auth/password-reset y /auth/password-recovery estan en la allowlist de SecurityConfig: los usa quien aun no puede iniciar sesion.
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PasswordResetService passwordResetService;

	@MockitoBean
	private PasswordRecoveryService passwordRecoveryService;

	@Test
	void resetPassword_conDatosValidos_retorna200SinToken() throws Exception {
		mockMvc.perform(post("/auth/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto("ana@promaty.com", "ABCD2345", "Nueva#2026"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.error").doesNotExist());

		verify(passwordResetService).resetPassword(any(PasswordResetDto.class));
	}

	@Test
	void resetPassword_conCodigoInvalido_retorna400ConNombreDeErrorPropio() throws Exception {
		doThrow(new InvalidResetCodeException("El codigo es invalido o expiro. Solicita uno nuevo."))
			.when(passwordResetService).resetPassword(any(PasswordResetDto.class));

		mockMvc.perform(post("/auth/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto("ana@promaty.com", "MALCODIGO", "Nueva#2026"))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.status").value(400))
			.andExpect(jsonPath("$.error.name").value("INVALID_RESET_CODE"))
			.andExpect(jsonPath("$.error.message").value("El codigo es invalido o expiro. Solicita uno nuevo."));
	}

	@Test
	void resetPassword_conContrasenaDebil_retorna400ConErrorEnElCampoPassword() throws Exception {
		mockMvc.perform(post("/auth/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto("ana@promaty.com", "ABCD2345", "debil"))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.password").exists());

		verifyNoInteractions(passwordResetService);
	}

	@Test
	void resetPassword_sinCodigoTemporal_retorna400ConErrorEnElCampoResetCode() throws Exception {
		mockMvc.perform(post("/auth/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto("ana@promaty.com", " ", "Nueva#2026"))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.resetCode").exists());
	}

	@Test
	void requestPasswordRecovery_conEmailValido_retorna200YDelegaAlServicio() throws Exception {
		mockMvc.perform(post("/auth/password-recovery")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ana@promaty.com\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.error").doesNotExist());

		verify(passwordRecoveryService).requestRecovery("ana@promaty.com");
	}

	@Test
	void requestPasswordRecovery_conEmailMalFormado_retorna400ConErrorEnElCampoEmail() throws Exception {
		mockMvc.perform(post("/auth/password-recovery")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"no-es-un-email\"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.email").exists());

		verifyNoInteractions(passwordRecoveryService);
	}

	private PasswordResetDto dto(String email, String resetCode, String password) {
		PasswordResetDto dto = new PasswordResetDto();
		dto.setEmail(email);
		dto.setResetCode(resetCode);
		dto.setPassword(password);
		return dto;
	}
}
