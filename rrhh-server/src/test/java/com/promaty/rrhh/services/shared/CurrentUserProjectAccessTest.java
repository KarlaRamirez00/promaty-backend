package com.promaty.rrhh.services.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.security.JwtPrincipal;

class CurrentUserProjectAccessTest {

	@AfterEach
	void limpiarContexto() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void sinAutenticacion_noPermiteTodosLosProyectosYListaVacia() {
		assertThat(CurrentUserProjectAccess.allowedAllProjects()).isFalse();
		assertThat(CurrentUserProjectAccess.projectIds()).isEmpty();
	}

	@Test
	void conAllowedAllProjects_devuelveTrue() {
		JwtPrincipal principal = new JwtPrincipal("1", "Admin", "Karla", List.of(), true, List.of());
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));

		assertThat(CurrentUserProjectAccess.allowedAllProjects()).isTrue();
	}

	@Test
	void conProjectIdsAsignados_devuelveLaListaDelPrincipal() {
		JwtPrincipal principal = new JwtPrincipal("1", "Supervisor", "Juan", List.of(), false, List.of(5L, 8L));
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));

		assertThat(CurrentUserProjectAccess.allowedAllProjects()).isFalse();
		assertThat(CurrentUserProjectAccess.projectIds()).containsExactly(5L, 8L);
	}
}
