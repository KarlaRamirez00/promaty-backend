package com.promaty.user.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.promaty.user.entity.OneTimeCode;
import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.entity.User;
import com.promaty.user.repository.OneTimeCodeRepository;
import com.promaty.user.repository.UserRepository;
import com.promaty.user.services.credential.OneTimeCodeGenerator;
import com.promaty.user.services.credential.OneTimeCodeIssuer;
import com.promaty.user.services.credential.TemporaryCodeIssuedEvent;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceImplTest {

	private static final long COOLDOWN_SECONDS = 60;
	private static final String EMAIL = "ana@promaty.com";
	private static final long USER_ID = 7L;

	@Mock
	private UserRepository userRepository;

	@Mock
	private OneTimeCodeRepository oneTimeCodeRepository;

	@Mock
	private OneTimeCodeGenerator oneTimeCodeGenerator;

	@Mock
	private OneTimeCodeIssuer oneTimeCodeIssuer;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private PasswordRecoveryServiceImpl service;
	private User user;

	@BeforeEach
	void setUp() {
		user = buildActiveUser();
		service = new PasswordRecoveryServiceImpl(
			userRepository, oneTimeCodeRepository, oneTimeCodeGenerator, oneTimeCodeIssuer,
			passwordEncoder, eventPublisher, COOLDOWN_SECONDS);
	}

	@Test
	void requestRecovery_usuarioActivoSinCodigoPrevio_emiteCodigoDeRecuperacionYPublicaElEvento() {
		stubActiveUser();
		stubPendingRecoveryCodes();
		when(oneTimeCodeGenerator.generate()).thenReturn("ABCD2345");
		when(passwordEncoder.encode("ABCD2345")).thenReturn("$2a$hash");

		service.requestRecovery(EMAIL);

		verify(oneTimeCodeIssuer).issue(user, OneTimeCodePurpose.RECOVERY, "$2a$hash");
		verify(eventPublisher).publishEvent(
			new TemporaryCodeIssuedEvent(EMAIL, "Ana", "ABCD2345", OneTimeCodePurpose.RECOVERY));
	}

	@Test
	void requestRecovery_emailInexistente_noHaceNada() {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

		service.requestRecovery(EMAIL);

		verifyNoInteractions(oneTimeCodeRepository, oneTimeCodeGenerator, oneTimeCodeIssuer, passwordEncoder, eventPublisher);
	}

	@Test
	void requestRecovery_usuarioInactivo_noHaceNada() {
		user.setActive(false);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

		service.requestRecovery(EMAIL);

		verifyNoInteractions(oneTimeCodeRepository, oneTimeCodeGenerator, oneTimeCodeIssuer, passwordEncoder, eventPublisher);
	}

	@Test
	void requestRecovery_dentroDelEnfriamiento_noEmiteOtroCodigo() {
		stubActiveUser();
		stubPendingRecoveryCodes(recoveryCodeIssuedSecondsAgo(COOLDOWN_SECONDS - 30));

		service.requestRecovery(EMAIL);

		verifyNoInteractions(oneTimeCodeGenerator, oneTimeCodeIssuer, passwordEncoder, eventPublisher);
		verify(oneTimeCodeRepository, never()).saveAll(any());
	}

	@Test
	void requestRecovery_pasadoElEnfriamiento_invalidaElCodigoAnteriorYEmiteUnoNuevo() {
		stubActiveUser();
		OneTimeCode previous = recoveryCodeIssuedSecondsAgo(COOLDOWN_SECONDS + 60);
		stubPendingRecoveryCodes(previous);
		when(oneTimeCodeGenerator.generate()).thenReturn("ABCD2345");
		when(passwordEncoder.encode("ABCD2345")).thenReturn("$2a$hash");

		service.requestRecovery(EMAIL);

		assertThat(previous.getUsedAt()).isNotNull();
		verify(oneTimeCodeRepository).saveAll(List.of(previous));
		verify(oneTimeCodeIssuer).issue(user, OneTimeCodePurpose.RECOVERY, "$2a$hash");
	}

	private void stubActiveUser() {
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
	}

	private void stubPendingRecoveryCodes(OneTimeCode... codes) {
		when(oneTimeCodeRepository.findByUser_IdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
			USER_ID, OneTimeCodePurpose.RECOVERY)).thenReturn(List.of(codes));
	}

	private OneTimeCode recoveryCodeIssuedSecondsAgo(long seconds) {
		OneTimeCode code = OneTimeCode.issue(
			user, OneTimeCodePurpose.RECOVERY, "hash", LocalDateTime.now().plusMinutes(30));
		code.setCreatedAt(LocalDateTime.now().minusSeconds(seconds));
		return code;
	}

	private User buildActiveUser() {
		User newUser = new User();
		newUser.setId(USER_ID);
		newUser.setEmail(EMAIL);
		newUser.setFirstName("Ana");
		newUser.setActive(true);
		return newUser;
	}
}
