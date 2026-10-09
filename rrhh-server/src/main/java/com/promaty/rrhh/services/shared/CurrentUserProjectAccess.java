package com.promaty.rrhh.services.shared;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.security.JwtPrincipal;

public final class CurrentUserProjectAccess {

	private CurrentUserProjectAccess() {
	}

	public static boolean allowedAllProjects() {
		JwtPrincipal principal = principal();
		return principal != null && principal.allowedAllProjects();
	}

	public static List<Long> projectIds() {
		JwtPrincipal principal = principal();
		return principal == null ? List.of() : principal.projectIds();
	}

	private static JwtPrincipal principal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
			return principal;
		}
		return null;
	}
}
