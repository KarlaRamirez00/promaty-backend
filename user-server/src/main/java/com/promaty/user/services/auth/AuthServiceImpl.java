package com.promaty.user.services.auth;

import java.time.Duration;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.contracts.auth.AuthOutcome;
import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;
import com.promaty.user.entity.LoginAttempt;
import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.User;
import com.promaty.user.repository.OneTimeCodeRepository;
import com.promaty.user.repository.UserProjectAccessRepository;
import com.promaty.user.repository.UserRepository;

@Service
public class AuthServiceImpl implements AuthService {

	private static final String ALLOWED_ALL_SUFFIX = ".AllowedAll";
	private static final String UNKNOWN_USER_PASSWORD = "promaty-unknown-user";

	private final UserRepository userRepository;
	private final UserProjectAccessRepository userProjectAccessRepository;
	private final OneTimeCodeRepository oneTimeCodeRepository;
	private final LoginAttemptTracker loginAttemptTracker;
	private final PasswordEncoder passwordEncoder;
	private final int maxFailedAttempts;
	private final Duration lockDuration;
	private final String unknownUserHash;

	public AuthServiceImpl(UserRepository userRepository, UserProjectAccessRepository userProjectAccessRepository,
			OneTimeCodeRepository oneTimeCodeRepository, LoginAttemptTracker loginAttemptTracker,
			PasswordEncoder passwordEncoder, @Value("${login.max-failed-attempts}") int maxFailedAttempts,
			@Value("${login.lock-minutes}") long lockMinutes) {
		this.userRepository = userRepository;
		this.userProjectAccessRepository = userProjectAccessRepository;
		this.oneTimeCodeRepository = oneTimeCodeRepository;
		this.loginAttemptTracker = loginAttemptTracker;
		this.passwordEncoder = passwordEncoder;
		this.maxFailedAttempts = maxFailedAttempts;
		this.lockDuration = Duration.ofMinutes(lockMinutes);
		this.unknownUserHash = passwordEncoder.encode(UNKNOWN_USER_PASSWORD);
	}

	// La fila de intentos queda bloqueada durante todo el metodo: intentos simultaneos del mismo email se procesan en serie.
	@Override
	@Transactional
	public AuthValidationResponseDto validate(AuthValidationRequestDto dto) {
		LoginAttempt attempt = loginAttemptTracker.acquire(dto.getEmail());
		Optional<User> user = userRepository.findByEmail(dto.getEmail());
		LocalDateTime now = LocalDateTime.now();
		// Se compara siempre, aun sin usuario o con la cuenta bloqueada, para que el tiempo de respuesta no delate nada.
		boolean passwordMatches = passwordEncoder.matches(dto.getPassword(), user.map(User::getPassword).orElse(unknownUserHash));
		if (attempt.isLocked(now)) {
			return AuthValidationResponseDto.locked(attempt.retryAfterSeconds(now));
		}
		if (passwordMatches && user.filter(User::getActive).isPresent()) {
			loginAttemptTracker.forget(dto.getEmail());
			return resolveResponse(user.get());
		}
		return rejectFailedAttempt(attempt, now);
	}

	private AuthValidationResponseDto rejectFailedAttempt(LoginAttempt attempt, LocalDateTime now) {
		attempt.registerFailure(now, maxFailedAttempts, lockDuration);
		if (attempt.isLocked(now)) {
			return AuthValidationResponseDto.locked(attempt.retryAfterSeconds(now));
		}
		return AuthValidationResponseDto.rejected(AuthOutcome.INVALID_CREDENTIALS);
	}

	private AuthValidationResponseDto resolveResponse(User user) {
		if (!user.getMustChangePassword()) {
			return buildAuthenticatedResponse(user);
		}
		return AuthValidationResponseDto.rejected(resolveTemporaryPasswordOutcome(user));
	}

	private AuthOutcome resolveTemporaryPasswordOutcome(User user) {
		LocalDateTime now = LocalDateTime.now();
		boolean codeStillValid = oneTimeCodeRepository
			.findFirstByUser_IdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(user.getId(), OneTimeCodePurpose.CREATION)
			.map(code -> !code.isExpired(now))
			.orElse(false);
		return codeStillValid ? AuthOutcome.PASSWORD_CHANGE_REQUIRED : AuthOutcome.TEMPORARY_PASSWORD_EXPIRED;
	}

	private AuthValidationResponseDto buildAuthenticatedResponse(User user) {
		List<String> permissions = user.getRole().getPermissions().stream()
			.map(Permission::getName)
			.toList();
		boolean allowedAllProjects = permissions.stream().anyMatch(name -> name.endsWith(ALLOWED_ALL_SUFFIX));
		List<Long> projectIds = allowedAllProjects
			? List.of()
			: userProjectAccessRepository.findProjectIdsByUserId(user.getId());
		String fullName = user.getFirstName() + " " + user.getLastName();
		boolean systemRole = Boolean.TRUE.equals(user.getRole().getSystem());
		return new AuthValidationResponseDto(AuthOutcome.AUTHENTICATED, user.getId(), user.getRole().getName(), fullName, permissions,
			allowedAllProjects, projectIds, systemRole, null);
	}
}
