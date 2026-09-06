package com.promaty.user.support;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Firma tokens JWT de prueba con el mismo secreto que se fija via @TestPropertySource. Evita
 * depender de la env var JWT_SECRET real y de la integracion @WithMockUser (que en Boot 4.1
 * @WebMvcTest ya no auto-configura).
 */
public final class TestJwt {

	public static final String SECRET =
		"clave-jwt-solo-para-tests-suficientemente-larga-para-cualquier-hmac-0123456789";

	private TestJwt() {
	}

	/** Header Authorization completo ("Bearer xxx") para un usuario con esos permisos. */
	public static String bearer(String... permissions) {
		String token = Jwts.builder()
			.subject("1")
			.claim("role", "Editor")
			.claim("permissions", List.of(permissions))
			.claim("allowedAllProjects", false)
			.claim("projectIds", List.of())
			.expiration(new Date(System.currentTimeMillis() + 60_000))
			.signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
			.compact();
		return "Bearer " + token;
	}
}
