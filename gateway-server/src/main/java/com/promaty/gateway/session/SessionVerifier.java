package com.promaty.gateway.session;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.contracts.auth.UserSessionStatusDto;
import com.promaty.security.JwtPrincipal;

@Component
public class SessionVerifier {

	private final SessionStatusCache cache;
	private final UserSessionClient client;

	public SessionVerifier(SessionStatusCache cache, UserSessionClient client) {
		this.cache = cache;
		this.client = client;
	}

	public boolean isCurrent(JwtPrincipal principal) {
		if (principal.sessionVersion() == null) {
			return false;
		}
		Optional<UserSessionStatusDto> cached = cache.get(principal.userId());
		if (cached.isPresent() && matches(cached.get(), principal)) {
			return true;
		}
		// Sin match en caché se consulta de nuevo: un login recién hecho trae una versión que la caché aún no conoce.
		return refresh(principal.userId())
			.map(status -> matches(status, principal))
			.orElse(false);
	}

	private Optional<UserSessionStatusDto> refresh(String userId) {
		Optional<UserSessionStatusDto> fetched = client.fetch(userId);
		fetched.ifPresent(status -> cache.put(userId, status));
		return fetched;
	}

	private boolean matches(UserSessionStatusDto status, JwtPrincipal principal) {
		return Boolean.TRUE.equals(status.getActive())
			&& principal.sessionVersion().equals(status.getSessionVersion());
	}
}
