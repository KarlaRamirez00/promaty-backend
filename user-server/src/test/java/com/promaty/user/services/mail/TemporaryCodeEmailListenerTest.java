package com.promaty.user.services.mail;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.exception.EmailSendException;
import com.promaty.user.services.credential.TemporaryCodeIssuedEvent;

@ExtendWith(MockitoExtension.class)
class TemporaryCodeEmailListenerTest {

	private static final TemporaryCodeIssuedEvent EVENT =
		new TemporaryCodeIssuedEvent("ana@promaty.com", "Ana", "ABCD2345", OneTimeCodePurpose.CREATION);
	private static final EmailMessage MESSAGE = new EmailMessage("ana@promaty.com", "asunto", "<p>cuerpo</p>");

	@Mock
	private EmailSender emailSender;

	@Mock
	private TemporaryCodeEmailBuilder emailBuilder;

	@InjectMocks
	private TemporaryCodeEmailListener listener;

	@Test
	void onTemporaryCodeIssued_enviaElCorreoArmadoPorElBuilder() {
		when(emailBuilder.build(EVENT)).thenReturn(MESSAGE);

		listener.onTemporaryCodeIssued(EVENT);

		verify(emailSender).send(MESSAGE);
	}

	@Test
	void onTemporaryCodeIssued_siFallaElEnvio_noPropagaLaExcepcion() {
		when(emailBuilder.build(EVENT)).thenReturn(MESSAGE);
		doThrow(new EmailSendException("No se pudo enviar el correo.", new RuntimeException()))
			.when(emailSender).send(MESSAGE);

		assertThatCode(() -> listener.onTemporaryCodeIssued(EVENT)).doesNotThrowAnyException();
	}
}
