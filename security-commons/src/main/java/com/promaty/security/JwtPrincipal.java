package com.promaty.security;

import java.util.List;

/** Datos del usuario de un JWT ya validado; queda como principal en el SecurityContext. */
public record JwtPrincipal(
	String userId,
	String role,
	String name,
	List<String> permissions,
	boolean allowedAllProjects,
	List<Long> projectIds
) {
}
