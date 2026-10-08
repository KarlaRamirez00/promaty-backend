package com.promaty.rrhh.services.shared;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.security.JwtPrincipal;

public final class CurrentUserId {

	private CurrentUserId() {
	}

	public static Long get() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
			return Long.valueOf(principal.userId());
		}
		return null;
	}
}
