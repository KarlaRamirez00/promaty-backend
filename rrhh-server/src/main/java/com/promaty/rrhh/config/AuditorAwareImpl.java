package com.promaty.rrhh.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.security.JwtPrincipal;

public class AuditorAwareImpl implements AuditorAware<String> {

	private static final String SISTEMA = "system";

	// "system" cubre lo que no tiene un JWT detras: arranque de la app, listeners, llamadas internas.
	@Override
	public Optional<String> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null
				&& authentication.getPrincipal() instanceof JwtPrincipal principal
				&& principal.name() != null) {
			return Optional.of(principal.name());
		}
		return Optional.of(SISTEMA);
	}
}
