package com.promaty.user.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InternalApiSecretValidatorTest {

	private static final String VALID_SECRET = "a".repeat(32);

	@Test
	void conSecretoDe32Caracteres_arranca() {
		assertThatCode(() -> new InternalApiSecretValidator(VALID_SECRET, "")).doesNotThrowAnyException();
	}

	@Test
	void sinSecreto_rechazaElArranque() {
		assertThatThrownBy(() -> new InternalApiSecretValidator("", ""))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("INTERNAL_API_SECRET");
	}

	@Test
	void conSecretoCorto_rechazaElArranque() {
		assertThatThrownBy(() -> new InternalApiSecretValidator("a".repeat(31), ""))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void conSecretoSiguienteCorto_rechazaElArranque() {
		assertThatThrownBy(() -> new InternalApiSecretValidator(VALID_SECRET, "corto"))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("INTERNAL_API_SECRET_NEXT");
	}

	@Test
	void conSecretoSiguienteValido_arranca() {
		assertThatCode(() -> new InternalApiSecretValidator(VALID_SECRET, "b".repeat(32))).doesNotThrowAnyException();
	}
}
