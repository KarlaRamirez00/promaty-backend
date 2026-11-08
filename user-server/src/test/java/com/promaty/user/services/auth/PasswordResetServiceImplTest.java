package com.promaty.user.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.promaty.user.dto.auth.PasswordResetDto;
import com.promaty.user.entity.OneTimeCode;
import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.entity.User;
import com.promaty.user.exception.InvalidResetCodeException;
import com.promaty.user.repository.OneTimeCodeRepository;
import com.promaty.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

	private static final int MAX_ATTEMPTS = 5;
	private static final String EMAIL = "ana@promaty.com";

	@Mock
	private UserRepository userRepository;

	@Mock
	private OneTimeCodeRepository oneTimeCodeRepository;

	@Mock
	private LoginAttemptTracker loginAttemptTracker;

	@Mock
	private PasswordEncoder passwordEncoder;

	private PasswordResetServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new PasswordResetServiceImpl(userRepository, oneTimeCodeRepository, loginAttemptTracker, passwordEncoder, MAX_ATTEMPTS);
	}

	@Test
	void resetPassword_conCodigoCorrecto_guardaLaClaveNuevaLimpiaMustChangePasswordYMarcaTodosLosCodigosUsados() {
		User user = temporaryPasswordUser();
		OneTimeCode latest = codeExpiringIn(30);
		OneTimeCode older = codeExpiringIn(10);
		stubUserAndCodes(user, latest, older);
		when(passwordEncoder.matches("ABCD2345", "hash")).thenReturn(true);
		when(passwordEncoder.encode("Nueva#2026")).thenReturn("$2a$nuevoHash");

		service.resetPassword(dto("ABCD2345", "Nueva#2026"));

		assertThat(user.getPassword()).isEqualTo("$2a$nuevoHash");
		assertThat(user.getMustChangePassword()).isFalse();
		verify(loginAttemptTracker).forget(EMAIL);
		assertThat(latest.getUsedAt()).isNotNull();
		assertThat(older.getUsedAt()).isNotNull();
		verify(userRepository).save(user);
		verify(oneTimeCodeRepository).saveAll(List.of(latest, older));
	}

	@Test
	void resetPassword_conCodigoIncorrecto_sumaUnIntentoYLanzaExcepcionSinCambiarLaClave() {
		User user = temporaryPasswordUser();
		OneTimeCode code = codeExpiringIn(30);
		stubUserAndCodes(user, code);
		when(passwordEncoder.matches("INCORRECTO", "hash")).thenReturn(false);

		assertThatThrownBy(() -> service.resetPassword(dto("INCORRECTO", "Nueva#2026")))
			.isInstanceOf(InvalidResetCodeException.class);

		assertThat(code.getAttempts()).isEqualTo(1);
		assertThat(code.getUsedAt()).isNull();
		assertThat(user.getMustChangePassword()).isTrue();
		verify(oneTimeCodeRepository).save(code);
		verify(userRepository, never()).save(user);
	}

	@Test
	void resetPassword_conCodigoVencido_lanzaExcepcionSinComprobarElCodigo() {
		User user = temporaryPasswordUser();
		stubUserAndCodes(user, codeExpiringIn(-1));

		assertThatThrownBy(() -> service.resetPassword(dto("ABCD2345", "Nueva#2026")))
			.isInstanceOf(InvalidResetCodeException.class);

		verifyNoInteractions(passwordEncoder);
	}

	@Test
	void resetPassword_alAgotarLosIntentos_rechazaInclusoElCodigoCorrecto() {
		User user = temporaryPasswordUser();
		OneTimeCode code = codeExpiringIn(30);
		for (int i = 0; i < MAX_ATTEMPTS; i++) {
			code.registerFailedAttempt();
		}
		stubUserAndCodes(user, code);

		assertThatThrownBy(() -> service.resetPassword(dto("ABCD2345", "Nueva#2026")))
			.isInstanceOf(InvalidResetCodeException.class);

		verifyNoInteractions(passwordEncoder);
		verify(userRepository, never()).save(user);
	}

	@Test
	void resetPassword_sinCodigosPendientes_lanzaExcepcion() {
		User user = temporaryPasswordUser();
		stubUserAndCodes(user);

		assertThatThrownBy(() -> service.resetPassword(dto("ABCD2345", "Nueva#2026")))
			.isInstanceOf(InvalidResetCodeException.class);
	}

	@Test
	void resetPassword_conEmailInexistente_lanzaLaMismaExcepcionSinConsultarCodigos() {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.resetPassword(dto("ABCD2345", "Nueva#2026")))
			.isInstanceOf(InvalidResetCodeException.class);

		verifyNoInteractions(oneTimeCodeRepository);
	}

	@Test
	void resetPassword_conUsuarioInactivo_lanzaLaMismaExcepcionSinConsultarCodigos() {
		User user = temporaryPasswordUser();
		user.setActive(false);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.resetPassword(dto("ABCD2345", "Nueva#2026")))
			.isInstanceOf(InvalidResetCodeException.class);

		verifyNoInteractions(oneTimeCodeRepository);
	}

	private void stubUserAndCodes(User user, OneTimeCode... codes) {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(oneTimeCodeRepository.findByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(1L)).thenReturn(List.of(codes));
	}

	private User temporaryPasswordUser() {
		User user = new User();
		user.setId(1L);
		user.setEmail(EMAIL);
		user.setActive(true);
		user.setMustChangePassword(true);
		user.setPassword("$2a$hashDeLaClaveTemporal");
		return user;
	}

	private OneTimeCode codeExpiringIn(long minutes) {
		return OneTimeCode.issue(new User(), OneTimeCodePurpose.CREATION, "hash", LocalDateTime.now().plusMinutes(minutes));
	}

	private PasswordResetDto dto(String resetCode, String password) {
		PasswordResetDto dto = new PasswordResetDto();
		dto.setEmail(EMAIL);
		dto.setResetCode(resetCode);
		dto.setPassword(password);
		return dto;
	}
}
