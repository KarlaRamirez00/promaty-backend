package com.promaty.user.services.credential;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class OneTimeCodeGeneratorTest {

	private static final int SAMPLES = 200;

	private final OneTimeCodeGenerator generator = new OneTimeCodeGenerator();

	@Test
	void generate_retornaCodigoDeLargoFijoConSoloCaracteresDelAlfabeto() {
		for (int i = 0; i < SAMPLES; i++) {
			String code = generator.generate();

			assertThat(code).hasSize(OneTimeCodeGenerator.LENGTH);
			assertThat(code).matches("[" + OneTimeCodeGenerator.ALPHABET + "]+");
		}
	}

	@Test
	void generate_noUsaCaracteresAmbiguos() {
		assertThat(OneTimeCodeGenerator.ALPHABET).doesNotContain("I", "O", "0", "1");
	}

	@Test
	void generate_retornaCodigosDistintosEnLlamadasSucesivas() {
		Set<String> codes = new HashSet<>();
		for (int i = 0; i < SAMPLES; i++) {
			codes.add(generator.generate());
		}

		assertThat(codes).hasSizeGreaterThan(SAMPLES - 5);
	}
}
