package com.promaty.authorizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Placeholder hasta que gateway-server este listo (ver docs/architecture.md).
 * Se permite cualquier request sin autenticacion por ahora.
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
