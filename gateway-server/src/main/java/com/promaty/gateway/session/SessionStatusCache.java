package com.promaty.gateway.session;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.promaty.contracts.auth.UserSessionStatusDto;

@Component
public class SessionStatusCache {

	private final Map<String, CachedStatus> entries = new ConcurrentHashMap<>();
	private final Duration ttl;
	private final Clock clock;

	@Autowired
	public SessionStatusCache(@Value("${session.cache-ttl-seconds}") long ttlSeconds) {
		this(Duration.ofSeconds(ttlSeconds), Clock.systemUTC());
	}

	SessionStatusCache(Duration ttl, Clock clock) {
		this.ttl = ttl;
		this.clock = clock;
	}

	public Optional<UserSessionStatusDto> get(String userId) {
		CachedStatus cached = entries.get(userId);
		if (cached == null || cached.isExpired(clock.instant())) {
			return Optional.empty();
		}
		return Optional.of(cached.status());
	}

	public void put(String userId, UserSessionStatusDto status) {
		entries.put(userId, new CachedStatus(status, clock.instant().plus(ttl)));
	}

	private record CachedStatus(UserSessionStatusDto status, Instant expiresAt) {

		boolean isExpired(Instant now) {
			return !now.isBefore(expiresAt);
		}
	}
}
