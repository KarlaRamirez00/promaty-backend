package com.promaty.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void invalidateSessions_subeLaVersionEnUno() {
		User user = new User();

		user.invalidateSessions();

		assertThat(user.getSessionVersion()).isEqualTo(1L);
	}

	@Test
	void toggleActive_alDesactivar_invalidaLasSesiones() {
		User user = new User();

		user.toggleActive();

		assertThat(user.getActive()).isFalse();
		assertThat(user.getSessionVersion()).isEqualTo(1L);
	}

	@Test
	void toggleActive_alReactivar_tambienInvalidaLasSesiones() {
		User user = new User();
		user.setActive(false);

		user.toggleActive();

		assertThat(user.getActive()).isTrue();
		assertThat(user.getSessionVersion()).isEqualTo(1L);
	}
}
