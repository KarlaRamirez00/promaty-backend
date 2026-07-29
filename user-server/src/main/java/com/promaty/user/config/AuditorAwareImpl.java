package com.promaty.user.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

/**
 * Placeholder hasta que exista usuario autenticado real (ver backend-ms.md, seccion 7).
 * Cuando authorizer-server/JWT esten integrados, leer el usuario desde el SecurityContext.
 */
public class AuditorAwareImpl implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {
		return Optional.of("system");
	}
}
