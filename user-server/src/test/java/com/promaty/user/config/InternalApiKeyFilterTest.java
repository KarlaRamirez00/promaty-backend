package com.promaty.user.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.contracts.auth.InternalApiHeader;

import jakarta.servlet.ServletException;

class InternalApiKeyFilterTest {

	private static final String SECRET = "secreto-actual-0123456789-abcdefghij";
	private static final String NEXT_SECRET = "secreto-nuevo-0123456789-abcdefghij";

	private final InternalApiKeyFilter filter = new InternalApiKeyFilter(SECRET, NEXT_SECRET);

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void conSecretoActual_autenticaComoServicioInterno() throws ServletException, IOException {
		Authentication authentication = runFilter("/internal/auth/validate", SECRET);

		assertThat(authentication).isNotNull();
		assertThat(authentication.getAuthorities()).extracting(Object::toString)
			.containsExactly(InternalApiKeyFilter.INTERNAL_SERVICE_AUTHORITY);
	}

	@Test
	void conSecretoSiguiente_tambienAutentica() throws ServletException, IOException {
		assertThat(runFilter("/internal/auth/validate", NEXT_SECRET)).isNotNull();
	}

	@Test
	void conSecretoIncorrecto_noAutentica() throws ServletException, IOException {
		assertThat(runFilter("/internal/auth/validate", "otro-secreto-0123456789-abcdefghijkl")).isNull();
	}

	@Test
	void sinCabecera_noAutentica() throws ServletException, IOException {
		assertThat(runFilter("/internal/auth/validate", null)).isNull();
	}

	@Test
	void conSecretoDeLargoDistinto_noAutentica() throws ServletException, IOException {
		assertThat(runFilter("/internal/auth/validate", "corto")).isNull();
	}

	@Test
	void sinSecretosConfigurados_rechazaTodo() throws ServletException, IOException {
		InternalApiKeyFilter sinSecretos = new InternalApiKeyFilter("", "");
		MockHttpServletRequest request = request("/internal/auth/validate", "");

		sinSecretos.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	void fueraDeInternal_noActuaAunqueLleveElSecreto() throws ServletException, IOException {
		assertThat(runFilter("/roles", SECRET)).isNull();
	}

	private Authentication runFilter(String path, String secret) throws ServletException, IOException {
		filter.doFilter(request(path, secret), new MockHttpServletResponse(), new MockFilterChain());
		return SecurityContextHolder.getContext().getAuthentication();
	}

	private MockHttpServletRequest request(String path, String secret) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
		if (secret != null) {
			request.addHeader(InternalApiHeader.SECRET, secret);
		}
		return request;
	}
}
