package com.promaty.rrhh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Placeholder hasta que gateway-server este listo (ver docs/architecture.md).
 * Se permite cualquier request sin autenticacion por ahora. Reemplazar por validacion de JWT +
 * @PreAuthorize por permiso.
 */
@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		return http
			// CSRF no aplica: API stateless con Bearer token, sin sesion basada en cookies.
			.csrf(csrf -> csrf.disable()) // NOSONAR (java:S4502)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.build();
	}
}
