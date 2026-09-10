package com.promaty.rrhh.support;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

// En Boot 4.1 @WebMvcTest no trae la integracion de spring-security-test (@WithMockUser no aplica),
// asi que cada test firma un Bearer real con el mismo secreto que fija @TestPropertySource.
public final class TestJwt {

	public static final String SECRET =
		"clave-jwt-solo-para-tests-suficientemente-larga-para-cualquier-hmac-0123456789";

	private TestJwt() {
	}

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
