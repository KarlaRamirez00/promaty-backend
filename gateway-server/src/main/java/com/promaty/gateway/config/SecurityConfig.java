package com.promaty.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(csrf -> csrf.disable()) // NOSONAR (java:S4502) - API stateless con Bearer token
			.cors(Customizer.withDefaults())
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			.build();
	}
}
