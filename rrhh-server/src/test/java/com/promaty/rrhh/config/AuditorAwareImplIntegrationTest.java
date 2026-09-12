package com.promaty.rrhh.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.entity.Client;
import com.promaty.rrhh.repository.ClientRepository;
import com.promaty.security.JwtPrincipal;

// Ejercita AuditorAwareImpl + JpaAuditingConfig juntos contra Hibernate real, no solo la clase en aislado.
// @Transactional en el test revierte el guardado al terminar, sin persistir datos en la BD real.
@SpringBootTest
@Transactional
class AuditorAwareImplIntegrationTest {

	@Autowired
	private ClientRepository clientRepository;

	@AfterEach
	void limpiarContexto() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void alGuardarUnRegistro_createdByQuedaConElNombreDelTokenActual() {
		JwtPrincipal principal = new JwtPrincipal("1", "Editor", "Ana Pérez", List.of(), false, List.of());
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));

		Client client = new Client();
		client.setName("Cliente de prueba auditoria " + System.nanoTime());

		Client guardado = clientRepository.save(client);

		assertThat(guardado.getCreatedBy()).isEqualTo("Ana Pérez");
	}
}
