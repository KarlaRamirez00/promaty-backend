package com.promaty.gateway.session;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.promaty.security.JwtPrincipal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Sin @Component a propósito: Boot lo registraría también como filtro de servlet y OncePerRequestFilter
// se saltaría la ejecución dentro de la cadena de seguridad.
public class SessionVersionFilter extends OncePerRequestFilter {

	private final SessionVerifier sessionVerifier;

	public SessionVersionFilter(SessionVerifier sessionVerifier) {
		this.sessionVerifier = sessionVerifier;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal
				&& !sessionVerifier.isCurrent(principal)) {
			SecurityContextHolder.clearContext();
		}
		filterChain.doFilter(request, response);
	}
}
