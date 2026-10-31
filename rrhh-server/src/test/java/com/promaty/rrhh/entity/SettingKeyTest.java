package com.promaty.rrhh.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SettingKeyTest {

	private static final SettingKey INTEGER_KEY = SettingKey.EXPIRING_CONTRACT_DAYS;

	@Test
	void validationError_enteroDentroDelRango_noRetornaError() {
		assertThat(INTEGER_KEY.validationError("30")).isEmpty();
	}

	@Test
	void validationError_enteroEnLosLimites_noRetornaError() {
		assertThat(INTEGER_KEY.validationError("1")).isEmpty();
		assertThat(INTEGER_KEY.validationError("365")).isEmpty();
	}

	@Test
	void validationError_valorBajoElMinimo_retornaErrorDeRango() {
		assertThat(INTEGER_KEY.validationError("0")).contains("Debe estar entre 1 y 365.");
	}

	@Test
	void validationError_valorSobreElMaximo_retornaErrorDeRango() {
		assertThat(INTEGER_KEY.validationError("366")).contains("Debe estar entre 1 y 365.");
	}

	@Test
	void validationError_textoNoNumerico_retornaErrorDeNumero() {
		assertThat(INTEGER_KEY.validationError("abc")).contains("Debe ser un número válido.");
	}

	@Test
	void validationError_decimalEnClaveEntera_retornaErrorDeEntero() {
		assertThat(INTEGER_KEY.validationError("7.5")).contains("Debe ser un número entero.");
	}

	@Test
	void validationError_decimalSinParteFraccionaria_seAceptaComoEntero() {
		assertThat(INTEGER_KEY.validationError("7.0")).isEmpty();
	}

	@Test
	void normalize_enteroConEspaciosYDecimalesInutiles_retornaFormaCanonica() {
		assertThat(INTEGER_KEY.normalize(" 7.0 ")).isEqualTo("7");
		assertThat(INTEGER_KEY.normalize("030")).isEqualTo("30");
	}

	@Test
	void fromName_claveExistente_retornaLaClave() {
		assertThat(SettingKey.fromName("EXPIRING_CONTRACT_DAYS")).contains(SettingKey.EXPIRING_CONTRACT_DAYS);
	}

	@Test
	void fromName_claveInexistente_retornaVacio() {
		assertThat(SettingKey.fromName("NO_EXISTE")).isEmpty();
	}
}
