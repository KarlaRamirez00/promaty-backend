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
import com.promaty.authorizer.exception.AccountLockedException;
import com.promaty.authorizer.exception.InvalidCredentialsException;
import com.promaty.authorizer.exception.PasswordChangeRequiredException;
import com.promaty.authorizer.exception.TemporaryPasswordExpiredException;
import com.promaty.contracts.auth.AuthOutcome;
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
		assertThat(claims.get("name", String.class)).isEqualTo("Ana Pérez");
		List<String> permissions = (List<String>) claims.get("permissions", List.class);
		assertThat(permissions).containsExactly("warehouse.read");
		assertThat(claims.get("allowedAllProjects", Boolean.class)).isFalse();
		// jjwt deserializa los claims desde JSON: un numero que cabe en int vuelve como
		// Integer, aunque en el DTO original era Long.
		List<Integer> projectIds = (List<Integer>) claims.get("projectIds", List.class);
		assertThat(projectIds).containsExactly(10, 20);
		assertThat(claims.get("isSystemRole", Boolean.class)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	void login_conRolDeSistema_incluyeIsSystemRoleTrueEnElToken() {
		LoginRequestDto dto = loginDto("ana@promaty.com", "clave123");
		AuthValidationResponseDto validacion = validacionValida();
		validacion.setSystemRole(true);
		when(userServerClient.validate(any())).thenReturn(BaseData.success(validacion));

		LoginResponseDto resultado = authService.login(dto);

		Claims claims = parseClaims(resultado.getToken());
		assertThat(claims.get("isSystemRole", Boolean.class)).isTrue();
	}

	@Test
	void login_conCredencialesInvalidas_lanzaInvalidCredentialsException() {
		LoginRequestDto dto = loginDto("ana@promaty.com", "claveIncorrecta");
		when(userServerClient.validate(any())).thenReturn(
			BaseData.success(AuthValidationResponseDto.rejected(AuthOutcome.INVALID_CREDENTIALS)));

		assertThatThrownBy(() -> authService.login(dto))
			.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void login_conClaveTemporalVigente_lanzaPasswordChangeRequiredExceptionSinEmitirToken() {
		LoginRequestDto dto = loginDto("ana@promaty.com", "ABCD2345");
		when(userServerClient.validate(any())).thenReturn(
			BaseData.success(AuthValidationResponseDto.rejected(AuthOutcome.PASSWORD_CHANGE_REQUIRED)));

		assertThatThrownBy(() -> authService.login(dto))
			.isInstanceOf(PasswordChangeRequiredException.class);
	}

	@Test
	void login_conClaveTemporalExpirada_lanzaTemporaryPasswordExpiredException() {
		LoginRequestDto dto = loginDto("ana@promaty.com", "ABCD2345");
		when(userServerClient.validate(any())).thenReturn(
			BaseData.success(AuthValidationResponseDto.rejected(AuthOutcome.TEMPORARY_PASSWORD_EXPIRED)));

		assertThatThrownBy(() -> authService.login(dto))
			.isInstanceOf(TemporaryPasswordExpiredException.class);
	}

	@Test
	void login_conCuentaBloqueada_lanzaAccountLockedExceptionConElTiempoRestante() {
		LoginRequestDto dto = loginDto("ana@promaty.com", "clave123");
		when(userServerClient.validate(any())).thenReturn(BaseData.success(AuthValidationResponseDto.locked(900)));

		assertThatThrownBy(() -> authService.login(dto))
			.isInstanceOfSatisfying(AccountLockedException.class, ex -> {
				assertThat(ex.getRetryAfterSeconds()).isEqualTo(900);
				assertThat(ex.getMessage()).isEqualTo("Demasiados intentos fallidos. Intenta de nuevo en 15 minutos.");
			});
	}

	@Test
	void login_conCuentaBloqueada_redondeaLosMinutosHaciaArribaYUsaSingularParaUno() {
		LoginRequestDto dto = loginDto("ana@promaty.com", "clave123");
		when(userServerClient.validate(any()))
			.thenReturn(BaseData.success(AuthValidationResponseDto.locked(61)))
			.thenReturn(BaseData.success(AuthValidationResponseDto.locked(60)));

		assertThatThrownBy(() -> authService.login(dto))
			.hasMessage("Demasiados intentos fallidos. Intenta de nuevo en 2 minutos.");
		assertThatThrownBy(() -> authService.login(dto))
			.hasMessage("Demasiados intentos fallidos. Intenta de nuevo en 1 minuto.");
	}

	private Claims parseClaims(String token) {
		SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	private AuthValidationResponseDto validacionValida() {
		AuthValidationResponseDto validacion = new AuthValidationResponseDto();
		validacion.setOutcome(AuthOutcome.AUTHENTICATED);
		validacion.setUserId(1L);
		validacion.setRole("Editor");
		validacion.setFullName("Ana Pérez");
		validacion.setPermissions(List.of("warehouse.read"));
		validacion.setAllowedAllProjects(false);
		validacion.setProjectIds(List.of(10L, 20L));
		validacion.setSystemRole(false);
		return validacion;
	}

	private LoginRequestDto loginDto(String email, String password) {
		LoginRequestDto dto = new LoginRequestDto();
		dto.setEmail(email);
		dto.setPassword(password);
		return dto;
	}
}
