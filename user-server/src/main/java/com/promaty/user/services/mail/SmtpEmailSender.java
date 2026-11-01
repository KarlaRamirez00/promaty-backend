package com.promaty.user.services.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.promaty.user.exception.EmailSendException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
@ConditionalOnProperty(name = "mail.enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

	private static final String ENCODING = "UTF-8";
	private static final String ERROR_ENVIO = "No se pudo enviar el correo.";

	private final JavaMailSender mailSender;
	private final String from;

	public SmtpEmailSender(JavaMailSender mailSender, @Value("${mail.from}") String from) {
		this.mailSender = mailSender;
		this.from = from;
	}

	@Override
	public void send(EmailMessage message) {
		try {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, ENCODING);
			helper.setFrom(from);
			helper.setTo(message.to());
			helper.setSubject(message.subject());
			helper.setText(message.htmlBody(), true);
			mailSender.send(mimeMessage);
		} catch (MessagingException | MailException e) {
			throw new EmailSendException(ERROR_ENVIO, e);
		}
	}
}
