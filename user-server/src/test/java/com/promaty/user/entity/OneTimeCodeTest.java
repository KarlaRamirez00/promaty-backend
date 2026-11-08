package com.promaty.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class OneTimeCodeTest {

	private static final int MAX_ATTEMPTS = 5;
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 1, 12, 0);

	@Test
	void issue_creaCodigoSinIntentosNiUso() {
		User user = new User();

		OneTimeCode code = OneTimeCode.issue(user, OneTimeCodePurpose.RECOVERY, "hash", NOW.plusMinutes(30));

		assertThat(code.getUser()).isSameAs(user);
		assertThat(code.getPurpose()).isEqualTo(OneTimeCodePurpose.RECOVERY);
		assertThat(code.getCodeHash()).isEqualTo("hash");
		assertThat(code.getExpiresAt()).isEqualTo(NOW.plusMinutes(30));
		assertThat(code.getAttempts()).isZero();
		assertThat(code.getUsedAt()).isNull();
	}

	@Test
	void isExpired_antesDelVencimientoRetornaFalse_enElInstanteExactoYDespuesRetornaTrue() {
		OneTimeCode code = vigente();

		assertThat(code.isExpired(NOW)).isFalse();
		assertThat(code.isExpired(NOW.plusMinutes(30))).isTrue();
		assertThat(code.isExpired(NOW.plusMinutes(31))).isTrue();
	}

	@Test
	void wasIssuedWithin_dentroDeLaVentanaRetornaTrue_enElLimiteExactoYDespuesRetornaFalse() {
		OneTimeCode code = vigente();
		code.setCreatedAt(NOW);
		Duration window = Duration.ofSeconds(60);

		assertThat(code.wasIssuedWithin(window, NOW.plusSeconds(59))).isTrue();
		assertThat(code.wasIssuedWithin(window, NOW.plusSeconds(60))).isFalse();
		assertThat(code.wasIssuedWithin(window, NOW.plusSeconds(61))).isFalse();
	}

	@Test
	void canBeRedeemed_codigoVigenteSinUsoYConIntentosDisponibles_retornaTrue() {
		OneTimeCode code = vigente();

		assertThat(code.canBeRedeemed(NOW, MAX_ATTEMPTS)).isTrue();
	}

	@Test
	void canBeRedeemed_codigoVencido_retornaFalse() {
		OneTimeCode code = vigente();

		assertThat(code.canBeRedeemed(NOW.plusMinutes(30), MAX_ATTEMPTS)).isFalse();
		assertThat(code.canBeRedeemed(NOW.plusMinutes(31), MAX_ATTEMPTS)).isFalse();
	}

	@Test
	void canBeRedeemed_codigoYaUsado_retornaFalse() {
		OneTimeCode code = vigente();
		code.markUsed(NOW);

		assertThat(code.getUsedAt()).isEqualTo(NOW);
		assertThat(code.canBeRedeemed(NOW, MAX_ATTEMPTS)).isFalse();
	}

	@Test
	void canBeRedeemed_alAlcanzarElMaximoDeIntentos_retornaFalse() {
		OneTimeCode code = vigente();
		for (int i = 0; i < MAX_ATTEMPTS - 1; i++) {
			code.registerFailedAttempt();
		}
		assertThat(code.canBeRedeemed(NOW, MAX_ATTEMPTS)).isTrue();

		code.registerFailedAttempt();

		assertThat(code.getAttempts()).isEqualTo(MAX_ATTEMPTS);
		assertThat(code.canBeRedeemed(NOW, MAX_ATTEMPTS)).isFalse();
	}

	private OneTimeCode vigente() {
		return OneTimeCode.issue(new User(), OneTimeCodePurpose.CREATION, "hash", NOW.plusMinutes(30));
	}
}
