package com.promaty.user.services.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mail.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEmailSender.class);

	public LoggingEmailSender() {
		LOGGER.warn("mail.enabled=false: los correos NO se envian, su contenido (incluidos los codigos) se escribe en el log. Solo para desarrollo.");
	}

	@Override
	public void send(EmailMessage message) {
		LOGGER.info("Correo simulado -> para: {} | asunto: {} | cuerpo: {}", message.to(), message.subject(), message.htmlBody());
	}
}
