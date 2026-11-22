package com.promaty.gateway.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.promaty.contracts.auth.UserSessionStatusDto;

class SessionStatusCacheTest {

	private static final Duration TTL = Duration.ofSeconds(10);

	private final MutableClock clock = new MutableClock();
	private final SessionStatusCache cache = new SessionStatusCache(TTL, clock);

	@Test
	void get_sinEntrada_retornaVacio() {
		assertThat(cache.get("7")).isEmpty();
	}

	@Test
	void get_dentroDelTtl_retornaElEstadoGuardado() {
		cache.put("7", new UserSessionStatusDto(true, 2L));
		clock.advance(TTL.minusSeconds(1));

		assertThat(cache.get("7")).hasValueSatisfying(status -> assertThat(status.getSessionVersion()).isEqualTo(2L));
	}

	@Test
	void get_alCumplirseElTtl_retornaVacio() {
		cache.put("7", new UserSessionStatusDto(true, 2L));
		clock.advance(TTL);

		assertThat(cache.get("7")).isEmpty();
	}

	private static final class MutableClock extends Clock {

		private Instant now = Instant.parse("2026-11-22T12:00:00Z");

		void advance(Duration duration) {
			now = now.plus(duration);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}
}
