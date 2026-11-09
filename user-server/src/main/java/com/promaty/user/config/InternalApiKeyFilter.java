package com.promaty.user.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.promaty.contracts.auth.InternalApiHeader;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class InternalApiKeyFilter extends OncePerRequestFilter {

	public static final String INTERNAL_SERVICE_AUTHORITY = "SERVICE_INTERNAL";

	private static final String INTERNAL_PATH_PREFIX = "/internal/";
	private static final String INTERNAL_SERVICE_PRINCIPAL = "internal-service";

	private final List<byte[]> acceptedSecrets;

	public InternalApiKeyFilter(String secret, String nextSecret) {
		this.acceptedSecrets = Stream.of(secret, nextSecret)
			.filter(StringUtils::hasText)
			.map(value -> value.getBytes(StandardCharsets.UTF_8))
			.toList();
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (isAccepted(request.getHeader(InternalApiHeader.SECRET))) {
			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				INTERNAL_SERVICE_PRINCIPAL, null, List.of(new SimpleGrantedAuthority(INTERNAL_SERVICE_AUTHORITY))));
		}
		chain.doFilter(request, response);
	}

	// Recorre todos los secretos aceptados sin cortar antes, para que el tiempo no delate cual coincidio.
	private boolean isAccepted(String provided) {
		if (provided == null) {
			return false;
		}
		byte[] providedBytes = provided.getBytes(StandardCharsets.UTF_8);
		boolean accepted = false;
		for (byte[] secret : acceptedSecrets) {
			accepted |= MessageDigest.isEqual(secret, providedBytes);
		}
		return accepted;
	}
}
