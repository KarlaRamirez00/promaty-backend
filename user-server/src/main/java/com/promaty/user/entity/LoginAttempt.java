package com.promaty.user.entity;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

// No extiende BaseDatedEntity: la clave es el email y la fila se crea con un INSERT nativo, sin auditoria JPA.
@Getter
@NoArgsConstructor
@Entity
@Table(name = "login_attempts")
public class LoginAttempt {

	private static final long MILLIS_PER_SECOND = 1000;

	@Id
	@Column(length = 254)
	private String email;

	@Column(nullable = false)
	private int failedAttempts;

	@Column
	private LocalDateTime lockedUntil;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public LoginAttempt(String email, LocalDateTime now) {
		this.email = email;
		this.updatedAt = now;
	}

	public boolean isLocked(LocalDateTime now) {
		return lockedUntil != null && now.isBefore(lockedUntil);
	}

	public void registerFailure(LocalDateTime now, int maxAttempts, Duration lockDuration) {
		updatedAt = now;
		if (isLocked(now)) {
			return;
		}
		failedAttempts++;
		if (failedAttempts >= maxAttempts) {
			lockedUntil = now.plus(lockDuration);
			failedAttempts = 0;
		}
	}

	public long retryAfterSeconds(LocalDateTime now) {
		if (!isLocked(now)) {
			return 0;
		}
		long remainingMillis = Duration.between(now, lockedUntil).toMillis();
		return Math.max(1, (remainingMillis + MILLIS_PER_SECOND - 1) / MILLIS_PER_SECOND);
	}
}
