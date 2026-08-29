package com.promaty.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Placeholder hasta que authorizer-server/gateway-server esten listos (ver docs/estado-proyecto.md).
 * Se permite cualquier request sin autenticacion por ahora. Reemplazar por validacion de JWT +
 * @PreAuthorize por permiso.
 */
@Configuration
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		return http
			// CSRF no aplica: API stateless con Bearer token, sin sesion basada en cookies.
			.csrf(csrf -> csrf.disable()) // NOSONAR (java:S4502)
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.build();
	}
}
