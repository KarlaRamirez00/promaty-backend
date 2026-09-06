package com.promaty.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Autentica por Bearer token: si es valido, deja el usuario en el SecurityContext con sus permisos
 * como authorities. Si falta o no es valido no escribe nada ni corta la request; el 401/403 lo da la
 * cadena de seguridad segun la regla del endpoint.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String HEADER = "Authorization";
	private static final String PREFIX = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String token = extractToken(request);
		if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			autenticarSiElTokenEsValido(token, request);
		}
		filterChain.doFilter(request, response);
	}

	private void autenticarSiElTokenEsValido(String token, HttpServletRequest request) {
		try {
			JwtPrincipal principal = jwtService.parse(token);
			List<SimpleGrantedAuthority> authorities = principal.permissions().stream()
				.map(SimpleGrantedAuthority::new)
				.toList();

			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(principal, null, authorities);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (JwtException ex) {
			// Token invalido/expirado: contexto vacio -> la cadena responde 401.
			SecurityContextHolder.clearContext();
		}
	}

	private String extractToken(HttpServletRequest request) {
		String header = request.getHeader(HEADER);
		if (header != null && header.startsWith(PREFIX)) {
			return header.substring(PREFIX.length());
		}
		return null;
	}
}
