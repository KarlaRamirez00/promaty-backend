package com.promaty.user.services.auth;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.user.config.AsyncConfig;
import com.promaty.user.entity.OneTimeCode;
import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.entity.User;
import com.promaty.user.repository.OneTimeCodeRepository;
import com.promaty.user.repository.UserRepository;
import com.promaty.user.services.credential.OneTimeCodeGenerator;
import com.promaty.user.services.credential.OneTimeCodeIssuer;
import com.promaty.user.services.credential.TemporaryCodeIssuedEvent;

@Service
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

	private final UserRepository userRepository;
	private final OneTimeCodeRepository oneTimeCodeRepository;
	private final OneTimeCodeGenerator oneTimeCodeGenerator;
	private final OneTimeCodeIssuer oneTimeCodeIssuer;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher eventPublisher;
	private final Duration cooldown;

	public PasswordRecoveryServiceImpl(
		UserRepository userRepository,
		OneTimeCodeRepository oneTimeCodeRepository,
		OneTimeCodeGenerator oneTimeCodeGenerator,
		OneTimeCodeIssuer oneTimeCodeIssuer,
		PasswordEncoder passwordEncoder,
		ApplicationEventPublisher eventPublisher,
		@Value("${one-time-code.recovery-cooldown-seconds}") long cooldownSeconds
	) {
		this.userRepository = userRepository;
		this.oneTimeCodeRepository = oneTimeCodeRepository;
		this.oneTimeCodeGenerator = oneTimeCodeGenerator;
		this.oneTimeCodeIssuer = oneTimeCodeIssuer;
		this.passwordEncoder = passwordEncoder;
		this.eventPublisher = eventPublisher;
		this.cooldown = Duration.ofSeconds(cooldownSeconds);
	}

	// Asincrono para que la respuesta HTTP tarde lo mismo exista o no el email (el hash BCrypt solo corre si existe).
	@Override
	@Async(AsyncConfig.RECOVERY_EXECUTOR)
	@Transactional
	public void requestRecovery(String email) {
		userRepository.findByEmail(email)
			.filter(User::getActive)
			.ifPresent(this::issueRecoveryCode);
	}

	private void issueRecoveryCode(User user) {
		LocalDateTime now = LocalDateTime.now();
		List<OneTimeCode> pendingCodes = oneTimeCodeRepository
			.findByUser_IdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(user.getId(), OneTimeCodePurpose.RECOVERY);
		if (isInCooldown(pendingCodes, now)) {
			return;
		}
		pendingCodes.forEach(pending -> pending.markUsed(now));
		oneTimeCodeRepository.saveAll(pendingCodes);

		String code = oneTimeCodeGenerator.generate();
		oneTimeCodeIssuer.issue(user, OneTimeCodePurpose.RECOVERY, passwordEncoder.encode(code));
		eventPublisher.publishEvent(new TemporaryCodeIssuedEvent(
			user.getEmail(), user.getFirstName(), code, OneTimeCodePurpose.RECOVERY));
	}

	private boolean isInCooldown(List<OneTimeCode> pendingCodes, LocalDateTime now) {
		return pendingCodes.stream()
			.findFirst()
			.filter(latest -> latest.wasIssuedWithin(cooldown, now))
			.isPresent();
	}
}
