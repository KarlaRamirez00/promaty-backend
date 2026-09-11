package com.promaty.rrhh.services.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.promaty.rrhh.dto.shared.Action;

class ActionsResolverTest {

	private final ActionsResolver resolver = new ActionsResolver();

	private static final Map<String, Action> REGLAS = Map.of(
		"client.update", Action.UPDATE,
		"client.active", Action.ACTIVE
	);

	@Test
	void resolve_conAmbosPermisos_devuelveAmbasAcciones() {
		List<Action> actions = resolver.resolve(Set.of("client.update", "client.active"), REGLAS);

		assertThat(actions).containsExactlyInAnyOrder(Action.UPDATE, Action.ACTIVE);
	}

	@Test
	void resolve_conUnSoloPermiso_devuelveSoloEsaAccion() {
		List<Action> actions = resolver.resolve(Set.of("client.update"), REGLAS);

		assertThat(actions).containsExactly(Action.UPDATE);
	}

	@Test
	void resolve_sinPermisos_devuelveListaVacia() {
		List<Action> actions = resolver.resolve(Set.of(), REGLAS);

		assertThat(actions).isEmpty();
	}

	@Test
	void resolve_conPermisoQueNoEstaEnLasReglas_loIgnora() {
		List<Action> actions = resolver.resolve(Set.of("client.read", "otro.permiso"), REGLAS);

		assertThat(actions).isEmpty();
	}
}
