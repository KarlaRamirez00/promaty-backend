package com.promaty.user.services.auth;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.user.repository.LoginAttemptRepository;

@Component
public class LoginAttemptCleanup {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoginAttemptCleanup.class);

	private final LoginAttemptRepository loginAttemptRepository;
	private final Duration retention;

	public LoginAttemptCleanup(
		LoginAttemptRepository loginAttemptRepository,
		@Value("${login.attempt-retention-hours}") long retentionHours
	) {
		this.loginAttemptRepository = loginAttemptRepository;
		this.retention = Duration.ofHours(retentionHours);
	}

	@Scheduled(cron = "${login.attempt-cleanup-cron}")
	@Transactional
	public void purgeInactive() {
		LocalDateTime now = LocalDateTime.now();
		int deleted = loginAttemptRepository.deleteInactive(now.minus(retention), now);
		if (deleted > 0) {
			LOGGER.info("Se eliminaron {} registros de intentos de login sin actividad.", deleted);
		}
	}
}
