package com.promaty.user.services.credential;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.promaty.user.entity.OneTimeCode;
import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.entity.User;
import com.promaty.user.repository.OneTimeCodeRepository;

@Component
public class OneTimeCodeIssuer {

	private final OneTimeCodeRepository oneTimeCodeRepository;
	private final long expirationMinutes;

	public OneTimeCodeIssuer(
		OneTimeCodeRepository oneTimeCodeRepository,
		@Value("${one-time-code.expiration-minutes}") long expirationMinutes
	) {
		this.oneTimeCodeRepository = oneTimeCodeRepository;
		this.expirationMinutes = expirationMinutes;
	}

	public OneTimeCode issue(User user, OneTimeCodePurpose purpose, String codeHash) {
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
		return oneTimeCodeRepository.save(OneTimeCode.issue(user, purpose, codeHash, expiresAt));
	}
}
