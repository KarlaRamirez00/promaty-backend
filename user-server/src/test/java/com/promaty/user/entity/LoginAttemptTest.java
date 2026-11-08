package com.promaty.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class LoginAttemptTest {

	private static final int MAX_ATTEMPTS = 3;
	private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
	private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 8, 12, 0);

	@Test
	void registerFailure_bajoElMaximo_sumaIntentosSinBloquear() {
		LoginAttempt attempt = new LoginAttempt("ana@promaty.com", NOW);

		attempt.registerFailure(NOW, MAX_ATTEMPTS, LOCK_DURATION);
		attempt.registerFailure(NOW, MAX_ATTEMPTS, LOCK_DURATION);

		assertThat(attempt.getFailedAttempts()).isEqualTo(2);
		assertThat(attempt.isLocked(NOW)).isFalse();
	}

	@Test
	void registerFailure_alAlcanzarElMaximo_bloqueaPorLaDuracionYReiniciaElContador() {
		LoginAttempt attempt = new LoginAttempt("ana@promaty.com", NOW);

		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			attempt.registerFailure(NOW, MAX_ATTEMPTS, LOCK_DURATION);
		}

		assertThat(attempt.getLockedUntil()).isEqualTo(NOW.plus(LOCK_DURATION));
		assertThat(attempt.getFailedAttempts()).isZero();
	}

	@Test
	void registerFailure_conLaCuentaYaBloqueada_noExtiendeElBloqueoPeroRenuevaLaActividad() {
		LoginAttempt attempt = new LoginAttempt("ana@promaty.com", NOW);
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			attempt.registerFailure(NOW, MAX_ATTEMPTS, LOCK_DURATION);
		}
		LocalDateTime later = NOW.plusMinutes(5);

		attempt.registerFailure(later, MAX_ATTEMPTS, LOCK_DURATION);

		assertThat(attempt.getLockedUntil()).isEqualTo(NOW.plus(LOCK_DURATION));
		assertThat(attempt.getFailedAttempts()).isZero();
		assertThat(attempt.getUpdatedAt()).isEqualTo(later);
	}

	@Test
	void isLocked_antesDelVencimientoRetornaTrue_enElInstanteExactoYDespuesRetornaFalse() {
		LoginAttempt attempt = lockedAt(NOW);

		assertThat(attempt.isLocked(NOW.plusMinutes(14))).isTrue();
		assertThat(attempt.isLocked(NOW.plusMinutes(15))).isFalse();
		assertThat(attempt.isLocked(NOW.plusMinutes(16))).isFalse();
	}

	@Test
	void isLocked_sinBloqueoRetornaFalse() {
		assertThat(new LoginAttempt("ana@promaty.com", NOW).isLocked(NOW)).isFalse();
	}

	@Test
	void retryAfterSeconds_conBloqueoRetornaLosSegundosRestantes() {
		LoginAttempt attempt = lockedAt(NOW);

		assertThat(attempt.retryAfterSeconds(NOW)).isEqualTo(900);
		assertThat(attempt.retryAfterSeconds(NOW.plusMinutes(10))).isEqualTo(300);
	}

	@Test
	void retryAfterSeconds_redondeaHaciaArribaYNuncaRetornaCeroMientrasEstaBloqueado() {
		LoginAttempt attempt = lockedAt(NOW);

		assertThat(attempt.retryAfterSeconds(NOW.plusMinutes(15).minusNanos(1_000_000))).isEqualTo(1);
	}

	@Test
	void retryAfterSeconds_sinBloqueoRetornaCero() {
		assertThat(new LoginAttempt("ana@promaty.com", NOW).retryAfterSeconds(NOW)).isZero();
	}

	private LoginAttempt lockedAt(LocalDateTime lockMoment) {
		LoginAttempt attempt = new LoginAttempt("ana@promaty.com", lockMoment);
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			attempt.registerFailure(lockMoment, MAX_ATTEMPTS, LOCK_DURATION);
		}
		return attempt;
	}
}
