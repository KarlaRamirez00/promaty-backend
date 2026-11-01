package com.promaty.user.entity;

import java.time.LocalDateTime;

import com.promaty.user.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "one_time_codes")
public class OneTimeCode extends BaseDatedEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OneTimeCodePurpose purpose;

	@Column(nullable = false)
	private String codeHash;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Column(nullable = false)
	private int attempts;

	@Column
	private LocalDateTime usedAt;

	public static OneTimeCode issue(User user, OneTimeCodePurpose purpose, String codeHash, LocalDateTime expiresAt) {
		OneTimeCode code = new OneTimeCode();
		code.user = user;
		code.purpose = purpose;
		code.codeHash = codeHash;
		code.expiresAt = expiresAt;
		return code;
	}

	public boolean canBeRedeemed(LocalDateTime now, int maxAttempts) {
		return usedAt == null && now.isBefore(expiresAt) && attempts < maxAttempts;
	}

	public void registerFailedAttempt() {
		attempts++;
	}

	public void markUsed(LocalDateTime now) {
		usedAt = now;
	}
}
