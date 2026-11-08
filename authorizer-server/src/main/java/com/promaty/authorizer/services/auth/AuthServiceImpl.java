package com.promaty.authorizer.services.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.promaty.authorizer.client.UserServerClient;
import com.promaty.authorizer.dto.auth.LoginRequestDto;
import com.promaty.authorizer.dto.auth.LoginResponseDto;
import com.promaty.authorizer.exception.AccountLockedException;
import com.promaty.authorizer.exception.InvalidCredentialsException;
import com.promaty.authorizer.exception.PasswordChangeRequiredException;
import com.promaty.authorizer.exception.TemporaryPasswordExpiredException;
import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthServiceImpl implements AuthService {

	private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Email o contrasena incorrectos.";
	private static final String MENSAJE_CUENTA_BLOQUEADA = "Demasiados intentos fallidos. Intenta de nuevo en %d %s.";
	private static final long SECONDS_PER_MINUTE = 60;
	private static final String MENSAJE_CAMBIO_DE_CLAVE_REQUERIDO = "Debes cambiar tu contrasena temporal antes de continuar.";
	private static final String MENSAJE_CLAVE_TEMPORAL_EXPIRADA = "Tu contrasena temporal expiro. Usa la opcion de recuperar contrasena para recibir una nueva.";

	private final UserServerClient userServerClient;
	private final SecretKey jwtSecretKey;
	private final long jwtExpirationMs;

	public AuthServiceImpl(
		UserServerClient userServerClient,
		@Value("${jwt.secret}") String jwtSecret,
		@Value("${jwt.expiration-ms}") long jwtExpirationMs
	) {
		this.userServerClient = userServerClient;
		this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		this.jwtExpirationMs = jwtExpirationMs;
	}

	@Override
	public LoginResponseDto login(LoginRequestDto dto) {
		AuthValidationResponseDto validacion = userServerClient
			.validate(new AuthValidationRequestDto(dto.getEmail(), dto.getPassword()))
			.getData();

		return switch (validacion.getOutcome()) {
			case AUTHENTICATED -> new LoginResponseDto(buildToken(validacion));
			case INVALID_CREDENTIALS -> throw new InvalidCredentialsException(MENSAJE_CREDENCIALES_INVALIDAS);
			case PASSWORD_CHANGE_REQUIRED -> throw new PasswordChangeRequiredException(MENSAJE_CAMBIO_DE_CLAVE_REQUERIDO);
			case TEMPORARY_PASSWORD_EXPIRED -> throw new TemporaryPasswordExpiredException(MENSAJE_CLAVE_TEMPORAL_EXPIRADA);
			case ACCOUNT_LOCKED -> throw new AccountLockedException(
				lockedMessage(validacion.getRetryAfterSeconds()), validacion.getRetryAfterSeconds());
		};
	}

	private String lockedMessage(long retryAfterSeconds) {
		long minutes = (retryAfterSeconds + SECONDS_PER_MINUTE - 1) / SECONDS_PER_MINUTE;
		return MENSAJE_CUENTA_BLOQUEADA.formatted(minutes, minutes == 1 ? "minuto" : "minutos");
	}

	private String buildToken(AuthValidationResponseDto validacion) {
		Instant ahora = Instant.now();
		Instant expiracion = ahora.plusMillis(jwtExpirationMs);

		return Jwts.builder()
			.subject(String.valueOf(validacion.getUserId()))
			.claim("role", validacion.getRole())
			.claim("name", validacion.getFullName())
			.claim("permissions", validacion.getPermissions())
			.claim("allowedAllProjects", validacion.getAllowedAllProjects())
			.claim("projectIds", validacion.getProjectIds())
			.claim("isSystemRole", validacion.getSystemRole())
			// jjwt solo acepta java.util.Date en su API, no Instant.
			.issuedAt(Date.from(ahora)) // NOSONAR
			.expiration(Date.from(expiracion)) // NOSONAR
			.signWith(jwtSecretKey)
			.compact();
	}
}
