package com.promaty.user.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.user.repository.LoginAttemptRepository;

@ExtendWith(MockitoExtension.class)
class LoginAttemptCleanupTest {

	private static final long RETENTION_HOURS = 24;

	@Mock
	private LoginAttemptRepository loginAttemptRepository;

	private LoginAttemptCleanup cleanup;

	@BeforeEach
	void setUp() {
		cleanup = new LoginAttemptCleanup(loginAttemptRepository, RETENTION_HOURS);
	}

	@Test
	void purgeInactive_borraLosRegistrosSinActividadDentroDeLaRetencion() {
		ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
		ArgumentCaptor<LocalDateTime> now = ArgumentCaptor.forClass(LocalDateTime.class);

		cleanup.purgeInactive();

		verify(loginAttemptRepository).deleteInactive(cutoff.capture(), now.capture());
		assertThat(Duration.between(cutoff.getValue(), now.getValue())).isEqualTo(Duration.ofHours(RETENTION_HOURS));
	}
}
