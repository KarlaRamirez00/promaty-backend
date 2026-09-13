package com.promaty.gateway.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.security.JwtAuthenticationFilter;
import com.promaty.security.JwtService;

@Configuration
public class SecurityConfig {

	private static final String[] RUTAS_PUBLICAS = {
		"/auth/**",
		"/actuator/health"
	};

	private final List<String> allowedOrigins;

	public SecurityConfig(@Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

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
	) throws Exception {
		return http
			.csrf(csrf -> csrf.disable()) // NOSONAR (java:S4502) - API stateless con Bearer token
			.cors(Customizer.withDefaults())
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
