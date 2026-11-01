package com.promaty.user.dto.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

class StrongPasswordValidatorTest {

	private final StrongPasswordValidator validator = new StrongPasswordValidator();
	private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
	private final ConstraintViolationBuilder builder = mock(ConstraintViolationBuilder.class);

	@Test
	void isValid_nulo_retornaTrueParaQueLoResuelvaNotBlank() {
		assertThat(validator.isValid(null, context)).isTrue();

		verifyNoInteractions(context);
	}

	@ParameterizedTest
	@ValueSource(strings = {"Clave1234!", "Abcdef1$", "Aa1!aaaaaaaa", "Contraseña1#"})
	void isValid_cumpleTodasLasReglas_retornaTrue(String password) {
		assertThat(validator.isValid(password, context)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"Aa1!aaa", "Aa1!aaaaaaaaa", "clave1234!", "CLAVE1234!", "ClaveClave!", "Clave12345"})
	void isValid_incumpleUnaRegla_retornaFalse(String password) {
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

		assertThat(validator.isValid(password, context)).isFalse();
	}

	@Test
	void isValid_sinMayuscula_mensajeNombraSoloLoQueFalta() {
		String mensaje = mensajeDeError("clave1234!");

		assertThat(mensaje).isEqualTo("La contrasena debe tener una mayuscula.");
	}

	@Test
	void isValid_incumpleVariasReglas_mensajeListaTodasLasFaltantes() {
		String mensaje = mensajeDeError("abc");

		assertThat(mensaje).isEqualTo(
			"La contrasena debe tener entre 8 y 12 caracteres, una mayuscula, un numero y un caracter especial.");
	}

	@Test
	void isValid_conEspaciosSinCaracterEspecial_noLosCuentaComoEspecial() {
		String mensaje = mensajeDeError("Clave 1234");

		assertThat(mensaje).isEqualTo("La contrasena debe tener un caracter especial.");
	}

	private String mensajeDeError(String password) {
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

		validator.isValid(password, context);

		verify(context).buildConstraintViolationWithTemplate(captor.capture());
		return captor.getValue();
	}
}
