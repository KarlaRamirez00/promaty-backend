package com.promaty.user.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.user.entity.LoginAttempt;
import com.promaty.user.repository.LoginAttemptRepository;

@ExtendWith(MockitoExtension.class)
class LoginAttemptTrackerTest {

	@Mock
	private LoginAttemptRepository loginAttemptRepository;

	@InjectMocks
	private LoginAttemptTracker tracker;

	@Test
	void acquire_reservaLaFilaYLaRetornaBloqueadaParaActualizar() {
		LoginAttempt attempt = new LoginAttempt("ana@promaty.com", LocalDateTime.now());
		when(loginAttemptRepository.findByEmailForUpdate("ana@promaty.com")).thenReturn(Optional.of(attempt));

		LoginAttempt resultado = tracker.acquire("ana@promaty.com");

		assertThat(resultado).isSameAs(attempt);
		InOrder order = inOrder(loginAttemptRepository);
		order.verify(loginAttemptRepository).touch(eq("ana@promaty.com"), any(LocalDateTime.class));
		order.verify(loginAttemptRepository).findByEmailForUpdate("ana@promaty.com");
	}

	@Test
	void acquire_normalizaElEmailParaQueLasVariantesCompartanElMismoContador() {
		LoginAttempt attempt = new LoginAttempt("ana@promaty.com", LocalDateTime.now());
		when(loginAttemptRepository.findByEmailForUpdate("ana@promaty.com")).thenReturn(Optional.of(attempt));

		tracker.acquire("  ANA@Promaty.COM ");

		verify(loginAttemptRepository).findByEmailForUpdate("ana@promaty.com");
	}

	@Test
	void acquire_siLaFilaNoAparece_lanzaIllegalStateException() {
		when(loginAttemptRepository.findByEmailForUpdate("ana@promaty.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> tracker.acquire("ana@promaty.com"))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void forget_borraElRegistroDelEmailNormalizado() {
		tracker.forget("  ANA@Promaty.COM ");

		verify(loginAttemptRepository).deleteByEmail("ana@promaty.com");
	}
}
