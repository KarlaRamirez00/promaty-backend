package com.promaty.rrhh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.security.JwtAuthenticationFilter;
import com.promaty.security.JwtService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private static final String[] RUTAS_PUBLICAS = {
		"/actuator/health",
		"/swagger-ui/**",
		"/v3/api-docs/**"
	};

	@Bean
	public JwtService jwtService(@Value("${jwt.secret}") String jwtSecret) {
		return new JwtService(jwtSecret);
	}

	@Bean
	public RestAuthErrorHandler restAuthErrorHandler(ObjectMapper objectMapper) {
		return new RestAuthErrorHandler(objectMapper);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtService jwtService,
		RestAuthErrorHandler restAuthErrorHandler
	) {
		return http
			// API stateless con Bearer token, sin sesion por cookies: CSRF no aplica.
			.csrf(csrf -> csrf.disable()) // NOSONAR (java:S4502)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(RUTAS_PUBLICAS).permitAll()
				.anyRequest().authenticated())
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(restAuthErrorHandler)
				.accessDeniedHandler(restAuthErrorHandler))
			// Un @Bean de tipo Filter lo auto-registraria Spring Boot como filtro de servlet suelto,
			// fuera de la cadena de seguridad; por eso se instancia a mano aca.
			.addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
