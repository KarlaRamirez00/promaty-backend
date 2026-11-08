package com.promaty.user.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.promaty.contracts.auth.AuthOutcome;
import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;
import com.promaty.user.entity.LoginAttempt;
import com.promaty.user.entity.OneTimeCode;
import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.repository.OneTimeCodeRepository;
import com.promaty.user.repository.UserProjectAccessRepository;
import com.promaty.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	private static final int MAX_FAILED_ATTEMPTS = 3;
	private static final long LOCK_MINUTES = 15;
	private static final String UNKNOWN_USER_HASH = "$2a$hashFicticio";

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserProjectAccessRepository userProjectAccessRepository;

	@Mock
	private OneTimeCodeRepository oneTimeCodeRepository;

	@Mock
	private LoginAttemptTracker loginAttemptTracker;

	@Mock
	private PasswordEncoder passwordEncoder;

	private AuthServiceImpl authService;
	private LoginAttempt attempt;

	@BeforeEach
	void setUp() {
		when(passwordEncoder.encode(anyString())).thenReturn(UNKNOWN_USER_HASH);
		authService = new AuthServiceImpl(
			userRepository, userProjectAccessRepository, oneTimeCodeRepository, loginAttemptTracker,
			passwordEncoder, MAX_FAILED_ATTEMPTS, LOCK_MINUTES);
		attempt = new LoginAttempt("ana@promaty.com", LocalDateTime.now());
		when(loginAttemptTracker.acquire(anyString())).thenReturn(attempt);
	}

	@Test
	void validate_conCredencialesValidas_retornaValidoConDatosDelUsuario() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		User user = usuarioActivo();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);
		when(userProjectAccessRepository.findProjectIdsByUserId(1L)).thenReturn(List.of(10L, 20L));

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.AUTHENTICATED);
		assertThat(resultado.getUserId()).isEqualTo(1L);
		assertThat(resultado.getRole()).isEqualTo("Editor");
		assertThat(resultado.getFullName()).isEqualTo("Ana Pérez");
		assertThat(resultado.getPermissions()).containsExactly("warehouse.read");
		assertThat(resultado.getAllowedAllProjects()).isFalse();
		assertThat(resultado.getProjectIds()).containsExactly(10L, 20L);
	}

	@Test
	void validate_conRolDeSistema_retornaSystemRoleTrue() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		User user = usuarioActivo();
		user.getRole().setSystem(true);
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);
		when(userProjectAccessRepository.findProjectIdsByUserId(1L)).thenReturn(List.of());

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getSystemRole()).isTrue();
	}

	@Test
	void validate_conRolNoDeSistema_retornaSystemRoleFalse() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		User user = usuarioActivo();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);
		when(userProjectAccessRepository.findProjectIdsByUserId(1L)).thenReturn(List.of());

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getSystemRole()).isFalse();
	}

	@Test
	void validate_conPermisoAllowedAll_retornaAllowedAllProjectsTrueYNoConsultaProyectos() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		User user = usuarioActivo();
		Permission allowedAll = new Permission();
		allowedAll.setName("warehouse.AllowedAll");
		user.getRole().setPermissions(Set.of(allowedAll));
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getAllowedAllProjects()).isTrue();
		assertThat(resultado.getProjectIds()).isEmpty();
		verify(userProjectAccessRepository, never()).findProjectIdsByUserId(eq(1L));
	}

	@Test
	void validate_conEmailInexistente_retornaInvalido() {
		AuthValidationRequestDto dto = requestDto("noexiste@promaty.com", "clave123");
		when(userRepository.findByEmail("noexiste@promaty.com")).thenReturn(Optional.empty());

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.INVALID_CREDENTIALS);
		assertThat(resultado.getUserId()).isNull();
	}

	@Test
	void validate_conPasswordIncorrecta_retornaInvalido() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "claveIncorrecta");
		User user = usuarioActivo();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("claveIncorrecta", user.getPassword())).thenReturn(false);

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.INVALID_CREDENTIALS);
	}

	@Test
	void validate_conUsuarioInactivo_retornaInvalido() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		User user = usuarioActivo();
		user.setActive(false);
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.INVALID_CREDENTIALS);
	}

	@Test
	void validate_conClaveTemporalVigente_retornaCambioDeClaveRequeridoSinDatosDeSesion() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "ABCD2345");
		User user = usuarioConClaveTemporal();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("ABCD2345", user.getPassword())).thenReturn(true);
		when(oneTimeCodeRepository.findFirstByUser_IdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
			1L, OneTimeCodePurpose.CREATION)).thenReturn(Optional.of(codigoQueVenceEn(30)));

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.PASSWORD_CHANGE_REQUIRED);
		assertThat(resultado.getPermissions()).isNull();
	}

	@Test
	void validate_conClaveTemporalExpirada_retornaClaveTemporalExpirada() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "ABCD2345");
		User user = usuarioConClaveTemporal();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("ABCD2345", user.getPassword())).thenReturn(true);
		when(oneTimeCodeRepository.findFirstByUser_IdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
			1L, OneTimeCodePurpose.CREATION)).thenReturn(Optional.of(codigoQueVenceEn(-1)));

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.TEMPORARY_PASSWORD_EXPIRED);
	}

	@Test
	void validate_conClaveTemporalSinCodigoPendiente_retornaClaveTemporalExpirada() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "ABCD2345");
		User user = usuarioConClaveTemporal();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("ABCD2345", user.getPassword())).thenReturn(true);
		when(oneTimeCodeRepository.findFirstByUser_IdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
			1L, OneTimeCodePurpose.CREATION)).thenReturn(Optional.empty());

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.TEMPORARY_PASSWORD_EXPIRED);
	}

	@Test
	void validate_conClaveTemporalIncorrecta_retornaCredencialesInvalidasSinConsultarCodigos() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "OTRACLAVE");
		User user = usuarioConClaveTemporal();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("OTRACLAVE", user.getPassword())).thenReturn(false);

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.INVALID_CREDENTIALS);
		verifyNoInteractions(oneTimeCodeRepository);
	}

	@Test
	void validate_conPasswordIncorrecta_sumaUnIntentoFallidoYNoReiniciaElRegistro() {
		User user = usuarioActivo();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("mala", user.getPassword())).thenReturn(false);

		authService.validate(requestDto("ana@promaty.com", "mala"));

		assertThat(attempt.getFailedAttempts()).isEqualTo(1);
		verify(loginAttemptTracker, never()).forget(anyString());
	}

	@Test
	void validate_elIntentoQueAlcanzaElMaximo_respondeBloqueadoConElTiempoRestante() {
		User user = usuarioActivo();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("mala", user.getPassword())).thenReturn(false);
		for (int i = 0; i < MAX_FAILED_ATTEMPTS - 1; i++) {
			authService.validate(requestDto("ana@promaty.com", "mala"));
		}

		AuthValidationResponseDto resultado = authService.validate(requestDto("ana@promaty.com", "mala"));

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.ACCOUNT_LOCKED);
		assertThat(resultado.getRetryAfterSeconds()).isBetween(LOCK_MINUTES * 60 - 5, LOCK_MINUTES * 60);
	}

	@Test
	void validate_conCuentaBloqueada_rechazaAunConLaClaveCorrectaYNoLaDesbloquea() {
		User user = usuarioActivo();
		bloquear(attempt);
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);

		AuthValidationResponseDto resultado = authService.validate(requestDto("ana@promaty.com", "clave123"));

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.ACCOUNT_LOCKED);
		assertThat(resultado.getRetryAfterSeconds()).isPositive();
		verify(loginAttemptTracker, never()).forget(anyString());
		verifyNoInteractions(userProjectAccessRepository, oneTimeCodeRepository);
	}

	@Test
	void validate_conCuentaBloqueada_compruebaLaClaveIgualParaNoDelatarElBloqueoPorTiempoDeRespuesta() {
		User user = usuarioActivo();
		bloquear(attempt);
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("cualquiera", user.getPassword())).thenReturn(false);

		authService.validate(requestDto("ana@promaty.com", "cualquiera"));

		verify(passwordEncoder).matches("cualquiera", user.getPassword());
	}

	@Test
	void validate_conIntentoFallidoDuranteElBloqueo_noExtiendeElBloqueo() {
		User user = usuarioActivo();
		bloquear(attempt);
		LocalDateTime lockedUntil = attempt.getLockedUntil();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("mala", user.getPassword())).thenReturn(false);

		authService.validate(requestDto("ana@promaty.com", "mala"));

		assertThat(attempt.getLockedUntil()).isEqualTo(lockedUntil);
		assertThat(attempt.getFailedAttempts()).isZero();
	}

	@Test
	void validate_conEmailInexistente_cuentaElIntentoYCompruebaContraUnHashFicticio() {
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.empty());

		AuthValidationResponseDto resultado = authService.validate(requestDto("ana@promaty.com", "clave123"));

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.INVALID_CREDENTIALS);
		assertThat(attempt.getFailedAttempts()).isEqualTo(1);
		verify(passwordEncoder).matches("clave123", UNKNOWN_USER_HASH);
	}

	@Test
	void validate_conEmailInexistente_tambienSeBloqueaAlLlegarAlMaximo() {
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.empty());
		AuthValidationResponseDto resultado = null;
		for (int i = 0; i < MAX_FAILED_ATTEMPTS; i++) {
			resultado = authService.validate(requestDto("ana@promaty.com", "clave123"));
		}

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.ACCOUNT_LOCKED);
	}

	@Test
	void validate_conLoginCorrecto_olvidaElRegistroDeIntentos() {
		User user = usuarioActivo();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);
		when(userProjectAccessRepository.findProjectIdsByUserId(1L)).thenReturn(List.of());

		AuthValidationResponseDto resultado = authService.validate(requestDto("ana@promaty.com", "clave123"));

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.AUTHENTICATED);
		verify(loginAttemptTracker).forget("ana@promaty.com");
	}

	@Test
	void validate_conBloqueoYaVencido_permiteIniciarSesion() {
		User user = usuarioActivo();
		LocalDateTime longAgo = LocalDateTime.now().minusMinutes(LOCK_MINUTES + 5);
		for (int i = 0; i < MAX_FAILED_ATTEMPTS; i++) {
			attempt.registerFailure(longAgo, MAX_FAILED_ATTEMPTS, Duration.ofMinutes(LOCK_MINUTES));
		}
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);
		when(userProjectAccessRepository.findProjectIdsByUserId(1L)).thenReturn(List.of());

		AuthValidationResponseDto resultado = authService.validate(requestDto("ana@promaty.com", "clave123"));

		assertThat(resultado.getOutcome()).isEqualTo(AuthOutcome.AUTHENTICATED);
	}

	private void bloquear(LoginAttempt target) {
		for (int i = 0; i < MAX_FAILED_ATTEMPTS; i++) {
			target.registerFailure(LocalDateTime.now(), MAX_FAILED_ATTEMPTS, Duration.ofMinutes(LOCK_MINUTES));
		}
	}

	private User usuarioConClaveTemporal() {
		User user = usuarioActivo();
		user.setMustChangePassword(true);
		return user;
	}

	private OneTimeCode codigoQueVenceEn(long minutes) {
		return OneTimeCode.issue(new User(), OneTimeCodePurpose.CREATION, "hash", LocalDateTime.now().plusMinutes(minutes));
	}

	private AuthValidationRequestDto requestDto(String email, String password) {
		AuthValidationRequestDto dto = new AuthValidationRequestDto();
		dto.setEmail(email);
		dto.setPassword(password);
		return dto;
	}

	private User usuarioActivo() {
		Permission permiso = new Permission();
		permiso.setName("warehouse.read");

		Role role = new Role();
		role.setName("Editor");
		role.setPermissions(Set.of(permiso));

		User user = new User();
		user.setId(1L);
		user.setFirstName("Ana");
		user.setLastName("Pérez");
		user.setEmail("ana@promaty.com");
		user.setPassword("$2a$10$hashSimulado");
		user.setActive(true);
		user.setRole(role);
		return user;
	}
}
