package com.promaty.gateway.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import com.promaty.contracts.auth.InternalApiHeader;
import com.promaty.contracts.auth.UserSessionStatusDto;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class UserSessionClientTest {

	private static final String SECRET = "secreto-interno-de-pruebas";
	private static final String USER_SERVICE_ID = "user-server";
	// Generoso a propósito: la primera petición de un HttpClient nuevo es lenta y no debe confundirse con un timeout.
	private static final long TIMEOUT_MS = 3000;

	private final LoadBalancerClient loadBalancerClient = mock(LoadBalancerClient.class);

	private HttpServer server;
	private volatile int status;
	private volatile String responseBody;
	private volatile String lastPath;
	private volatile String lastSecret;

	@BeforeEach
	void startServer() throws IOException {
		status = 200;
		responseBody = "{\"data\":{\"active\":true,\"sessionVersion\":3},\"error\":null}";
		lastPath = null;
		lastSecret = null;

		server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
		server.createContext("/", this::handle);
		server.start();

		ServiceInstance instance = mock(ServiceInstance.class);
		when(instance.getUri()).thenReturn(URI.create("http://127.0.0.1:" + server.getAddress().getPort()));
		when(loadBalancerClient.choose(USER_SERVICE_ID)).thenReturn(instance);
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
	}

	@Test
	void fetch_respuestaExitosa_retornaElEstadoYEnviaElSecretoInterno() {
		Optional<UserSessionStatusDto> result = client().fetch("7");

		assertThat(result).hasValueSatisfying(sessionStatus -> {
			assertThat(sessionStatus.getActive()).isTrue();
			assertThat(sessionStatus.getSessionVersion()).isEqualTo(3L);
		});
		assertThat(lastPath).isEqualTo("/internal/users/7/session");
		assertThat(lastSecret).isEqualTo(SECRET);
	}

	@Test
	void fetch_usuarioNoExiste_retornaVacio() {
		status = 404;
		responseBody = "{\"data\":null,\"error\":{\"status\":404}}";

		assertThat(client().fetch("7")).isEmpty();
		assertThat(lastPath).isEqualTo("/internal/users/7/session");
	}

	@Test
	void fetch_errorDeUserServer_retornaVacio() {
		status = 500;
		responseBody = "{}";

		assertThat(client().fetch("7")).isEmpty();
		assertThat(lastPath).isEqualTo("/internal/users/7/session");
	}

	@Test
	void fetch_sinInstanciaRegistradaEnEureka_retornaVacio() {
		when(loadBalancerClient.choose(USER_SERVICE_ID)).thenReturn(null);

		assertThat(client().fetch("7")).isEmpty();
		assertThat(lastPath).isNull();
	}

	@Test
	void fetch_userServerApagado_retornaVacio() {
		server.stop(0);

		assertThat(client().fetch("7")).isEmpty();
	}

	private UserSessionClient client() {
		return new UserSessionClient(loadBalancerClient, SECRET, TIMEOUT_MS);
	}

	private void handle(HttpExchange exchange) throws IOException {
		lastPath = exchange.getRequestURI().getPath();
		lastSecret = exchange.getRequestHeaders().getFirst(InternalApiHeader.SECRET);
		byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, body.length);
		try (OutputStream output = exchange.getResponseBody()) {
			output.write(body);
		}
	}
}
