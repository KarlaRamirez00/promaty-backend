package com.promaty.user.services.auth;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.promaty.user.entity.LoginAttempt;
import com.promaty.user.repository.LoginAttemptRepository;

@Component
public class LoginAttemptTracker {

	private final LoginAttemptRepository loginAttemptRepository;

	public LoginAttemptTracker(LoginAttemptRepository loginAttemptRepository) {
		this.loginAttemptRepository = loginAttemptRepository;
	}

	// Debe llamarse dentro de una transaccion: el bloqueo de la fila dura hasta que esta termina.
	public LoginAttempt acquire(String email) {
		String key = normalize(email);
		loginAttemptRepository.touch(key, LocalDateTime.now());
		return loginAttemptRepository.findByEmailForUpdate(key)
			.orElseThrow(() -> new IllegalStateException("No se pudo reservar el registro de intentos de login."));
	}

	public void forget(String email) {
		loginAttemptRepository.deleteByEmail(normalize(email));
	}

	private String normalize(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
