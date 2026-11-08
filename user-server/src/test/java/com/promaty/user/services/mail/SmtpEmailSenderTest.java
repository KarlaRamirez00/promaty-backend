package com.promaty.user.services.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import com.promaty.user.exception.EmailSendException;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class SmtpEmailSenderTest {

	private static final String FROM = "no-reply@promaty.local";

	@Mock
	private JavaMailSender mailSender;

	private SmtpEmailSender sender;
	private MimeMessage mimeMessage;

	@BeforeEach
	void setUp() {
		sender = new SmtpEmailSender(mailSender, FROM);
		mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
	}

	@Test
	void send_armaElMensajeConRemitenteDestinatarioAsuntoYCuerpoHtml() throws Exception {
		sender.send(new EmailMessage("ana@promaty.cl", "Creación de usuario en Promaty", "<p>Hola</p>"));

		verify(mailSender).send(mimeMessage);
		assertThat(mimeMessage.getFrom()[0]).hasToString(FROM);
		assertThat(mimeMessage.getAllRecipients()[0]).hasToString("ana@promaty.cl");
		assertThat(mimeMessage.getSubject()).isEqualTo("Creación de usuario en Promaty");
		assertThat(mimeMessage.getContent()).hasToString("<p>Hola</p>");
	}

	@Test
	void send_conImagenesIncrustadas_lasAdjuntaConSuContentId() throws Exception {
		InlineImage logo = new InlineImage("logo-test", new ByteArrayResource(new byte[] {1, 2, 3}), "image/png");

		sender.send(new EmailMessage("ana@promaty.cl", "Asunto", "<img src=\"cid:logo-test\">", List.of(logo)));

		mimeMessage.saveChanges();
		ByteArrayOutputStream raw = new ByteArrayOutputStream();
		mimeMessage.writeTo(raw);
		assertThat(raw.toString(StandardCharsets.UTF_8))
			.contains("Content-ID: <logo-test>")
			.contains("image/png");
		verify(mailSender).send(mimeMessage);
	}

	@Test
	void send_fallaElServidorSmtp_lanzaEmailSendException() {
		doThrow(new MailSendException("smtp caido")).when(mailSender).send(any(MimeMessage.class));

		assertThatThrownBy(() -> sender.send(new EmailMessage("ana@promaty.cl", "Asunto", "<p>Hola</p>")))
			.isInstanceOf(EmailSendException.class)
			.hasCauseInstanceOf(MailSendException.class);
	}

	@Test
	void send_destinatarioConFormatoInvalido_lanzaEmailSendException() {
		assertThatThrownBy(() -> sender.send(new EmailMessage("no es un email", "Asunto", "<p>Hola</p>")))
			.isInstanceOf(EmailSendException.class);
	}
}
