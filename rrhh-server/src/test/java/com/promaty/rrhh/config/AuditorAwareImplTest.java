package com.promaty.rrhh.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.security.JwtPrincipal;

class AuditorAwareImplTest {

	private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

	@AfterEach
	void limpiarContexto() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getCurrentAuditor_sinAutenticacion_devuelveSystem() {
		assertThat(auditorAware.getCurrentAuditor()).contains("system");
	}

	@Test
	void getCurrentAuditor_conJwtPrincipalConNombre_devuelveElNombre() {
		JwtPrincipal principal = new JwtPrincipal("1", "Editor", "Ana Pérez", List.of(), false, List.of());
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));

		assertThat(auditorAware.getCurrentAuditor()).contains("Ana Pérez");
	}

	@Test
	void getCurrentAuditor_conJwtPrincipalSinNombre_devuelveSystem() {
		JwtPrincipal principal = new JwtPrincipal("1", "Editor", null, List.of(), false, List.of());
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));

		assertThat(auditorAware.getCurrentAuditor()).contains("system");
	}
}
