package com.promaty.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class InternalApiSecretValidator {

	private static final int MIN_SECRET_LENGTH = 32;

	public InternalApiSecretValidator(@Value("${internal-api.secret:}") String secret,
			@Value("${internal-api.next-secret:}") String nextSecret) {
		requireStrong(secret, "INTERNAL_API_SECRET");
		if (StringUtils.hasText(nextSecret)) {
			requireStrong(nextSecret, "INTERNAL_API_SECRET_NEXT");
		}
	}

	private void requireStrong(String value, String variableName) {
		if (!StringUtils.hasText(value) || value.length() < MIN_SECRET_LENGTH) {
			throw new IllegalStateException(
				variableName + " es obligatoria y debe tener al menos " + MIN_SECRET_LENGTH + " caracteres.");
		}
	}
}
