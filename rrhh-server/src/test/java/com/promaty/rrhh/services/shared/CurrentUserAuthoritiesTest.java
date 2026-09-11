package com.promaty.rrhh.services.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentUserAuthoritiesTest {

	@AfterEach
	void limpiarContexto() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void get_sinAutenticacion_devuelveVacio() {
		assertThat(CurrentUserAuthorities.get()).isEmpty();
	}

	@Test
	void get_conAutenticacion_devuelveLasAuthoritiesComoStrings() {
		var authorities = List.of(new SimpleGrantedAuthority("client.read"), new SimpleGrantedAuthority("client.update"));
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken("user", null, authorities));

		assertThat(CurrentUserAuthorities.get()).isEqualTo(Set.of("client.read", "client.update"));
	}
}
