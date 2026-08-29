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
import com.promaty.authorizer.exception.InvalidCredentialsException;
import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthServiceImpl implements AuthService {

	private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Email o contrasena incorrectos.";

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

		if (!Boolean.TRUE.equals(validacion.getValid())) {
			throw new InvalidCredentialsException(MENSAJE_CREDENCIALES_INVALIDAS);
		}

		return new LoginResponseDto(buildToken(validacion));
	}

	private String buildToken(AuthValidationResponseDto validacion) {
		Instant ahora = Instant.now();
		Instant expiracion = ahora.plusMillis(jwtExpirationMs);

		return Jwts.builder()
			.subject(String.valueOf(validacion.getUserId()))
			.claim("role", validacion.getRole())
			.claim("permissions", validacion.getPermissions())
			.claim("allowedAllProjects", validacion.getAllowedAllProjects())
			.claim("projectIds", validacion.getProjectIds())
			// jjwt solo acepta java.util.Date en su API, no Instant.
			.issuedAt(Date.from(ahora)) // NOSONAR
			.expiration(Date.from(expiracion)) // NOSONAR
			.signWith(jwtSecretKey)
			.compact();
	}
}
