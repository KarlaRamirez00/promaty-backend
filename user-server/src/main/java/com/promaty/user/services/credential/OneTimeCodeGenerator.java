package com.promaty.user.services.credential;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class OneTimeCodeGenerator {

	// Sin I, O, 0 ni 1: el usuario transcribe el codigo desde el correo y esos caracteres se confunden.
	static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	static final int LENGTH = 8;

	private final SecureRandom random = new SecureRandom();

	public String generate() {
		StringBuilder code = new StringBuilder(LENGTH);
		for (int i = 0; i < LENGTH; i++) {
			code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
		}
		return code.toString();
	}
}
