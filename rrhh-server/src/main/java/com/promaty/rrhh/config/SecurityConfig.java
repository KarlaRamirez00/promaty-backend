package com.promaty.rrhh.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Placeholder hasta que gateway-server este listo (ver docs/architecture.md).
 * Se permite cualquier request sin autenticacion por ahora. Reemplazar por validacion de JWT +
 * @PreAuthorize por permiso.
 */
@Configuration
public class SecurityConfig {

	private final List<String> allowedOrigins;

	public SecurityConfig(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		return http
			// CSRF no aplica: API stateless con Bearer token, sin sesion basada en cookies.
			.csrf(csrf -> csrf.disable()) // NOSONAR (java:S4502)
			.cors(Customizer.withDefaults())
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.build();
	}

	/**
	 * Sin esto el preflight OPTIONS del navegador nunca recibe los headers Access-Control-*, y el
	 * browser bloquea la respuesta real aunque el endpoint sea permitAll() - CORS lo decide el
	 * navegador, no Spring Security.
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuracion = new CorsConfiguration();
		configuracion.setAllowedOrigins(allowedOrigins);
		configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuracion);
		return source;
	}
}
