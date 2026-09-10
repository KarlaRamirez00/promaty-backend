package com.promaty.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtService {

	private final SecretKey secretKey;

	public JwtService(String jwtSecret) {
		this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	/** Lanza io.jsonwebtoken.JwtException si el token no es de fiar (firma, expiracion, formato). */
	public JwtPrincipal parse(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();

		return new JwtPrincipal(
			claims.getSubject(),
			claims.get("role", String.class),
			claims.get("name", String.class),
			readStringList(claims, "permissions"),
			Boolean.TRUE.equals(claims.get("allowedAllProjects", Boolean.class)),
			readLongList(claims, "projectIds")
		);
	}

	private List<String> readStringList(Claims claims, String key) {
		Object value = claims.get(key);
		if (value instanceof List<?> list) {
			return list.stream().map(String::valueOf).toList();
		}
		return List.of();
	}

	// jjwt deserializa los claims numericos como Integer, no Long (ver rbac.md): normalizar via Number.
	private List<Long> readLongList(Claims claims, String key) {
		Object value = claims.get(key);
		if (value instanceof List<?> list) {
			return list.stream().map(item -> ((Number) item).longValue()).toList();
		}
		return List.of();
	}
}
