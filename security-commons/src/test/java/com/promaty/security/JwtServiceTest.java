package com.promaty.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

	private static final String SECRET =
		"clave-secreta-de-pruebas-suficientemente-larga-para-cualquier-hmac-0123456789";

	private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
	private final JwtService jwtService = new JwtService(SECRET);

	@Test
	void parse_conTokenValido_devuelveTodosLosClaims() {
		String token = Jwts.builder()
			.subject("42")
			.claim("role", "Editor")
			.claim("name", "Ana Pérez")
			.claim("permissions", List.of("rrhh.read", "rrhh.write"))
			.claim("allowedAllProjects", false)
			.claim("projectIds", List.of(1, 2, 3))
			.signWith(key)
			.compact();

		JwtPrincipal principal = jwtService.parse(token);

		assertThat(principal.userId()).isEqualTo("42");
		assertThat(principal.role()).isEqualTo("Editor");
		assertThat(principal.name()).isEqualTo("Ana Pérez");
		assertThat(principal.permissions()).containsExactly("rrhh.read", "rrhh.write");
		assertThat(principal.allowedAllProjects()).isFalse();
		assertThat(principal.projectIds()).containsExactly(1L, 2L, 3L);
	}

	@Test
	void parse_conAllowedAllProjectsTrue_loRefleja() {
		String token = Jwts.builder()
			.subject("1")
			.claim("allowedAllProjects", true)
			.signWith(key)
			.compact();

		assertThat(jwtService.parse(token).allowedAllProjects()).isTrue();
	}

	@Test
	void parse_conFirmaDeOtraClave_lanzaJwtException() {
		SecretKey otraClave = Keys.hmacShaKeyFor(
			"otra-clave-distinta-igual-de-larga-para-que-siga-siendo-valida-0123456789".getBytes(StandardCharsets.UTF_8));
		String token = Jwts.builder().subject("1").signWith(otraClave).compact();

		assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void parse_conTokenExpirado_lanzaJwtException() {
		String token = Jwts.builder()
			.subject("1")
			.issuedAt(new Date(System.currentTimeMillis() - 120_000))
			.expiration(new Date(System.currentTimeMillis() - 60_000))
			.signWith(key)
			.compact();

		assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void parse_sinClaimsOpcionales_devuelveListasVaciasYAllowedAllFalse() {
		String token = Jwts.builder().subject("7").signWith(key).compact();

		JwtPrincipal principal = jwtService.parse(token);

		assertThat(principal.permissions()).isEmpty();
		assertThat(principal.projectIds()).isEmpty();
		assertThat(principal.allowedAllProjects()).isFalse();
		assertThat(principal.role()).isNull();
		assertThat(principal.name()).isNull();
	}

	@Test
	void parse_conProjectIdsComoInteger_losNormalizaALong() {
		String token = Jwts.builder()
			.subject("1")
			.claim("projectIds", List.of(824, 900))
			.signWith(key)
			.compact();

		assertThat(jwtService.parse(token).projectIds()).containsExactly(824L, 900L);
	}
}
