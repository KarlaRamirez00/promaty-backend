package com.promaty.user.services.credential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.user.entity.OneTimeCode;
import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.entity.User;
import com.promaty.user.repository.OneTimeCodeRepository;

@ExtendWith(MockitoExtension.class)
class OneTimeCodeIssuerTest {

	private static final long EXPIRATION_MINUTES = 30;

	@Mock
	private OneTimeCodeRepository oneTimeCodeRepository;

	@Test
	void issue_guardaElCodigoConElHashElPropositoYLaExpiracionConfigurada() {
		OneTimeCodeIssuer issuer = new OneTimeCodeIssuer(oneTimeCodeRepository, EXPIRATION_MINUTES);
		User user = new User();
		when(oneTimeCodeRepository.save(any(OneTimeCode.class))).thenAnswer(call -> call.getArgument(0));
		LocalDateTime before = LocalDateTime.now();

		issuer.issue(user, OneTimeCodePurpose.CREATION, "hash");

		LocalDateTime after = LocalDateTime.now();
		ArgumentCaptor<OneTimeCode> captor = ArgumentCaptor.forClass(OneTimeCode.class);
		verify(oneTimeCodeRepository).save(captor.capture());
		OneTimeCode saved = captor.getValue();
		assertThat(saved.getUser()).isSameAs(user);
		assertThat(saved.getPurpose()).isEqualTo(OneTimeCodePurpose.CREATION);
		assertThat(saved.getCodeHash()).isEqualTo("hash");
		assertThat(saved.getExpiresAt()).isBetween(
			before.plusMinutes(EXPIRATION_MINUTES), after.plusMinutes(EXPIRATION_MINUTES));
	}
}
