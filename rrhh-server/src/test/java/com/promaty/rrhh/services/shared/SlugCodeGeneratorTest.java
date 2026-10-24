package com.promaty.rrhh.services.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SlugCodeGeneratorTest {

	@Test
	void fromName_conPreposicionesYArticulos_losDescarta() {
		String code = SlugCodeGenerator.fromName("Robo al interior de las dependencias", 40);

		assertThat(code).isEqualTo("ROBO_INTERIOR_DEPENDENCIAS");
	}

	@Test
	void fromName_conConjuncionesYPronombres_tambienLosDescarta() {
		String code = SlugCodeGenerator.fromName("Daño negligente de herramientas o equipos", 40);

		assertThat(code).isEqualTo("DANO_NEGLIGENTE_HERRAMIENTAS_EQUIPOS");
	}

	@Test
	void fromName_masLargoQueElMaximo_truncaPorPalabraCompletaSinCortarla() {
		String code = SlugCodeGenerator.fromName("Incumplimiento grave y reiterado de las normas internas de seguridad", 20);

		assertThat(code).isEqualTo("INCUMPLIMIENTO_GRAVE");
	}

	@Test
	void fromName_primeraPalabraYaSuperaElMaximo_laTruncaPorCaracter() {
		String code = SlugCodeGenerator.fromName("Superacondicionamiento", 10);

		assertThat(code).isEqualTo("SUPERACOND");
	}

	@Test
	void fromName_conCaracteresNoAlfanumericosRepetidos_colapsaAUnSoloGuionBajo() {
		String code = SlugCodeGenerator.fromName("Robo  /  hurto", 40);

		assertThat(code).isEqualTo("ROBO_HURTO");
	}
}
