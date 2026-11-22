package com.promaty.gateway.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.contracts.auth.UserSessionStatusDto;
import com.promaty.security.JwtPrincipal;

@ExtendWith(MockitoExtension.class)
class SessionVerifierTest {

	private static final String USER_ID = "7";

	@Mock
	private SessionStatusCache cache;

	@Mock
	private UserSessionClient client;

	@InjectMocks
	private SessionVerifier verifier;

	@Test
	void isCurrent_tokenSinSessionVersion_rechazaSinConsultar() {
		assertThat(verifier.isCurrent(principalWithVersion(null))).isFalse();

		verifyNoInteractions(cache, client);
	}

	@Test
	void isCurrent_cacheConLaMismaVersion_aceptaSinConsultarAUserServer() {
		when(cache.get(USER_ID)).thenReturn(Optional.of(new UserSessionStatusDto(true, 2L)));

		assertThat(verifier.isCurrent(principalWithVersion(2L))).isTrue();

		verifyNoInteractions(client);
	}

	@Test
	void isCurrent_loginRecienHechoConCacheDesactualizada_reconsultaYAcepta() {
		UserSessionStatusDto fresh = new UserSessionStatusDto(true, 3L);
		when(cache.get(USER_ID)).thenReturn(Optional.of(new UserSessionStatusDto(true, 2L)));
		when(client.fetch(USER_ID)).thenReturn(Optional.of(fresh));

		assertThat(verifier.isCurrent(principalWithVersion(3L))).isTrue();

		verify(cache).put(USER_ID, fresh);
	}

	@Test
	void isCurrent_versionDelTokenAnteriorALaActual_rechaza() {
		when(client.fetch(USER_ID)).thenReturn(Optional.of(new UserSessionStatusDto(true, 3L)));

		assertThat(verifier.isCurrent(principalWithVersion(2L))).isFalse();
	}

	@Test
	void isCurrent_usuarioInactivo_rechazaAunqueLaVersionCoincida() {
		when(client.fetch(USER_ID)).thenReturn(Optional.of(new UserSessionStatusDto(false, 2L)));

		assertThat(verifier.isCurrent(principalWithVersion(2L))).isFalse();
	}

	@Test
	void isCurrent_consultaFallida_rechazaSinGuardarEnCache() {
		when(client.fetch(USER_ID)).thenReturn(Optional.empty());

		assertThat(verifier.isCurrent(principalWithVersion(2L))).isFalse();

		verify(cache, never()).put(any(), any());
	}

	private JwtPrincipal principalWithVersion(Long sessionVersion) {
		return new JwtPrincipal(USER_ID, "Editor", "Ana Pérez", List.of(), false, List.of(), false, sessionVersion);
	}
}
