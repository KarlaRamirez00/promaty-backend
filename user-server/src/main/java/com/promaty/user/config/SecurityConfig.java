package com.promaty.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.security.JwtAuthenticationFilter;
import com.promaty.security.JwtService;

/**
 * El JWT lo emite authorizer-server en el login; aca solo se valida localmente (firma + expiracion)
 * contra la misma clave, sin llamar a authorizer (ver docs/rbac.md). @PreAuthorize por permiso vive
 * en cada controller.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private static final String[] RUTAS_PUBLICAS = {
		"/internal/**",
		"/actuator/health",
		"/swagger-ui/**",
		"/v3/api-docs/**"
	};

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public JwtService jwtService(@Value("${jwt.secret}") String jwtSecret) {
		return new JwtService(jwtSecret);
	}

	@Bean
	public RestAuthErrorHandler restAuthErrorHandler(ObjectMapper objectMapper) {
		return new RestAuthErrorHandler(objectMapper);
	}

	// JwtAuthenticationFilter se instancia aca, no como @Bean: un @Bean de tipo Filter lo auto-registra
	// Spring Boot como filtro de servlet suelto (corre fuera de la cadena de seguridad, y su guard de
	// OncePerRequestFilter luego impide que corra dentro, donde importa).
	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtService jwtService,
		RestAuthErrorHandler restAuthErrorHandler
	) {
		return http
			// CSRF no aplica: API stateless con Bearer token, sin sesion basada en cookies.
			.csrf(csrf -> csrf.disable()) // NOSONAR (java:S4502)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(RUTAS_PUBLICAS).permitAll()
				.anyRequest().authenticated())
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(restAuthErrorHandler)
				.accessDeniedHandler(restAuthErrorHandler))
			.addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
