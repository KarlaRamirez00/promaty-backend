package com.promaty.user.services.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.promaty.user.exception.EmailSendException;
import com.promaty.user.services.credential.TemporaryCodeIssuedEvent;

@Component
public class TemporaryCodeEmailListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryCodeEmailListener.class);

	private final EmailSender emailSender;
	private final TemporaryCodeEmailBuilder emailBuilder;

	public TemporaryCodeEmailListener(EmailSender emailSender, TemporaryCodeEmailBuilder emailBuilder) {
		this.emailSender = emailSender;
		this.emailBuilder = emailBuilder;
	}

	// Un fallo de SMTP no debe tumbar la peticion: el usuario ya quedo creado y puede pedir un codigo nuevo.
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onTemporaryCodeIssued(TemporaryCodeIssuedEvent event) {
		try {
			emailSender.send(emailBuilder.build(event));
		} catch (EmailSendException e) {
			LOGGER.error("No se pudo enviar el correo con la clave temporal a {}", event.email(), e);
		}
	}
}
