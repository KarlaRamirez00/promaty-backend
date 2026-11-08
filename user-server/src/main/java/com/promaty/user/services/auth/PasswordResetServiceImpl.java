package com.promaty.user.services.auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.user.dto.auth.PasswordResetDto;
import com.promaty.user.entity.OneTimeCode;
import com.promaty.user.entity.User;
import com.promaty.user.exception.InvalidResetCodeException;
import com.promaty.user.repository.OneTimeCodeRepository;
import com.promaty.user.repository.UserRepository;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

	private static final String INVALID_CODE_MESSAGE = "El codigo es invalido o expiro. Solicita uno nuevo.";

	private final UserRepository userRepository;
	private final OneTimeCodeRepository oneTimeCodeRepository;
	private final LoginAttemptTracker loginAttemptTracker;
	private final PasswordEncoder passwordEncoder;
	private final int maxAttempts;

	public PasswordResetServiceImpl(
		UserRepository userRepository,
		OneTimeCodeRepository oneTimeCodeRepository,
		LoginAttemptTracker loginAttemptTracker,
		PasswordEncoder passwordEncoder,
		@Value("${one-time-code.max-attempts}") int maxAttempts
	) {
		this.userRepository = userRepository;
		this.oneTimeCodeRepository = oneTimeCodeRepository;
		this.loginAttemptTracker = loginAttemptTracker;
		this.passwordEncoder = passwordEncoder;
		this.maxAttempts = maxAttempts;
	}

	// noRollbackFor: el intento fallido se cuenta con save() y la excepcion que sigue haria rollback del contador.
	@Override
	@Transactional(noRollbackFor = InvalidResetCodeException.class)
	public void resetPassword(PasswordResetDto dto) {
		User user = userRepository.findByEmail(dto.getEmail())
			.filter(User::getActive)
			.orElseThrow(() -> new InvalidResetCodeException(INVALID_CODE_MESSAGE));
		List<OneTimeCode> pendingCodes = oneTimeCodeRepository.findByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(user.getId());
		LocalDateTime now = LocalDateTime.now();
		OneTimeCode code = latestRedeemable(pendingCodes, now)
			.orElseThrow(() -> new InvalidResetCodeException(INVALID_CODE_MESSAGE));

		if (!passwordEncoder.matches(dto.getResetCode(), code.getCodeHash())) {
			code.registerFailedAttempt();
			oneTimeCodeRepository.save(code);
			throw new InvalidResetCodeException(INVALID_CODE_MESSAGE);
		}

		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setMustChangePassword(false);
		loginAttemptTracker.forget(user.getEmail());
		pendingCodes.forEach(pending -> pending.markUsed(now));
		userRepository.save(user);
		oneTimeCodeRepository.saveAll(pendingCodes);
	}

	private Optional<OneTimeCode> latestRedeemable(List<OneTimeCode> pendingCodes, LocalDateTime now) {
		return pendingCodes.stream()
			.findFirst()
			.filter(code -> code.canBeRedeemed(now, maxAttempts));
	}
}
