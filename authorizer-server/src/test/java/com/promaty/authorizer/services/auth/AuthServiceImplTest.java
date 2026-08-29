package com.promaty.authorizer.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.authorizer.client.UserServerClient;
import com.promaty.authorizer.dto.auth.LoginRequestDto;
import com.promaty.authorizer.dto.auth.LoginResponseDto;
import com.promaty.authorizer.dto.response.BaseData;
import com.promaty.authorizer.exception.InvalidCredentialsException;
import com.promaty.contracts.auth.AuthValidationResponseDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	private static final String TEST_SECRET = "0123456789012345678901234567890123456789012345";
	private static final long TEST_EXPIRATION_MS = 3_600_000L;

	@Mock
	private UserServerClient userServerClient;

	private AuthServiceImpl authService;

	@BeforeEach
	void setUp() {
		authService = new AuthServiceImpl(userServerClient, TEST_SECRET, TEST_EXPIRATION_MS);
	}

	@Test
	@SuppressWarnings("unchecked")
	void login_conCredencialesValidas_retornaTokenConClaimsDelUsuario() {
		LoginRequestDto dto = loginDto("ana@promaty.com", "clave123");
		when(userServerClient.validate(any())).thenReturn(BaseData.success(validacionValida()));

		LoginResponseDto resultado = authService.login(dto);

		assertThat(resultado.getToken()).isNotBlank();
		Claims claims = parseClaims(resultado.getToken());
		assertThat(claims.getSubject()).isEqualTo("1");
		assertThat(claims.get("role", String.class)).isEqualTo("Editor");
		List<String> permissions = (List<String>) claims.get("permissions", List.class);
		assertThat(permissions).containsExactly("warehouse.read");
		assertThat(claims.get("allowedAllProjects", Boolean.class)).isFalse();
		// jjwt deserializa los claims desde JSON: un numero que cabe en int vuelve como
		// Integer, aunque en el DTO original era Long.
		List<Integer> projectIds = (List<Integer>) claims.get("projectIds", List.class);
		assertThat(projectIds).containsExactly(10, 20);
	}

	@Test
	void login_conCredencialesInvalidas_lanzaInvalidCredentialsException() {
		LoginRequestDto dto = loginDto("ana@promaty.com", "claveIncorrecta");
		AuthValidationResponseDto invalida = new AuthValidationResponseDto();
		invalida.setValid(false);
		when(userServerClient.validate(any())).thenReturn(BaseData.success(invalida));

		assertThatThrownBy(() -> authService.login(dto))
			.isInstanceOf(InvalidCredentialsException.class);
	}

	private Claims parseClaims(String token) {
		SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	private AuthValidationResponseDto validacionValida() {
		AuthValidationResponseDto validacion = new AuthValidationResponseDto();
		validacion.setValid(true);
		validacion.setUserId(1L);
		validacion.setRole("Editor");
		validacion.setPermissions(List.of("warehouse.read"));
		validacion.setAllowedAllProjects(false);
		validacion.setProjectIds(List.of(10L, 20L));
		return validacion;
	}

	private LoginRequestDto loginDto(String email, String password) {
		LoginRequestDto dto = new LoginRequestDto();
		dto.setEmail(email);
		dto.setPassword(password);
		return dto;
	}
}
