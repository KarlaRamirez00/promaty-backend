package com.promaty.rrhh.services.dashboard.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.security.JwtPrincipal;

class DashboardScopeTest {

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void of_conProyectoFueraDelAccesoDelUsuario_lanzaAccessDenied() {
		autenticar(false, List.of(1L, 2L));

		assertThatThrownBy(() -> DashboardScope.of(9L)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void of_conProyectoAsignadoAlUsuario_noCubreTodosLosProyectos() {
		autenticar(false, List.of(1L, 2L));

		assertThat(DashboardScope.of(2L).coversEveryProject()).isFalse();
	}

	@Test
	void of_sinProyectoYConAccesoTotal_cubreTodosLosProyectos() {
		autenticar(true, List.of());

		assertThat(DashboardScope.of(null).coversEveryProject()).isTrue();
	}

	@Test
	void of_conProyectoElegidoYAccesoTotal_noCubreTodosLosProyectos() {
		autenticar(true, List.of());

		assertThat(DashboardScope.of(9L).coversEveryProject()).isFalse();
	}

	private void autenticar(boolean allowedAllProjects, List<Long> projectIds) {
		JwtPrincipal principal = new JwtPrincipal("1", "Editor", "Ana Pérez", List.of(), allowedAllProjects, projectIds, false);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
	}
}
