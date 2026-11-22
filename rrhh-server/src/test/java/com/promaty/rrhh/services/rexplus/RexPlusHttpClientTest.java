package com.promaty.rrhh.services.rexplus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.promaty.rrhh.config.RexPlusHttpConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class RexPlusHttpClientTest {

	private static final String API_KEY = "test-key";
	private static final String GENERIC_FAILURE = "Rex+ no respondió.";
	// Generosos a propósito: la primera petición de un HttpClient nuevo es lenta y no debe confundirse con un timeout.
	private static final long CONNECT_TIMEOUT_MS = 2000;
	private static final long READ_TIMEOUT_MS = 3000;
	private static final long SHORT_READ_TIMEOUT_MS = 300;
	private static final long SLOW_RESPONSE_MS = 1500;

	private HttpServer server;
	private volatile int status;
	private volatile String responseBody;
	private volatile long delayMs;
	private volatile String lastPath;
	private volatile String lastApiKey;
	private volatile String lastBody;

	@BeforeEach
	void startServer() throws IOException {
		status = 201;
		responseBody = "{\"referenceNumber\":\"REX-000001\"}";
		delayMs = 0;
		lastPath = null;
		lastApiKey = null;
		lastBody = null;

		server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
		server.createContext("/", this::handle);
		server.start();
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
	}

	@Test
	void syncContract_respuestaExitosa_retornaLaReferenciaYEnviaApiKeyYJson() {
		RexSyncResult result = client().syncContract(RexPlusTestData.contract());

		assertThat(result.success()).isTrue();
		assertThat(result.externalReferenceNumber()).isEqualTo("REX-000001");
		assertThat(lastPath).isEqualTo("/api/v1/contracts");
		assertThat(lastApiKey).isEqualTo(API_KEY);
		assertThat(lastBody)
			.contains("\"externalId\":\"10\"")
			.contains("\"staffRut\":\"12.345.678-9\"")
			.contains("\"startDate\":\"2026-12-01\"")
			.contains("\"projectCode\":\"7\"");
	}

	@Test
	void syncTermination_respuestaExitosa_retornaLaReferenciaDeLaTerminacion() {
		responseBody = "{\"referenceNumber\":\"REX-TERM-000001\"}";

		RexSyncResult result = client().syncTermination(RexPlusTestData.termination());

		assertThat(result.success()).isTrue();
		assertThat(result.externalReferenceNumber()).isEqualTo("REX-TERM-000001");
		assertThat(lastPath).isEqualTo("/api/v1/terminations");
		assertThat(lastBody)
			.contains("\"contractReferenceNumber\":\"REX-000001\"")
			.contains("\"legalCause\":\"ART_159_4\"");
	}

	@Test
	void annulTermination_conReferencia_llamaAlEndpointDeAnulacionConElMotivo() {
		status = 200;
		responseBody = "{\"referenceNumber\":\"REX-TERM-000001\"}";
		var termination = RexPlusTestData.termination();
		termination.setRexTerminationNumber("REX-TERM-000001");

		RexSyncResult result = client().annulTermination(termination, "Error de digitación");

		assertThat(result.success()).isTrue();
		assertThat(result.externalReferenceNumber()).isEqualTo("REX-TERM-000001");
		assertThat(lastPath).isEqualTo("/api/v1/terminations/REX-TERM-000001/annulment");
		assertThat(lastBody).contains("\"reason\":\"Error de digitación\"");
	}

	@Test
	void annulTermination_sinReferenciaEnRex_fallaSinLlamarAlServicio() {
		RexSyncResult result = client().annulTermination(RexPlusTestData.termination(), "motivo");

		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).isNotBlank();
		assertThat(lastPath).isNull();
	}

	@Test
	void annulContract_conReferencia_llamaAlEndpointDeAnulacionConElMotivo() {
		status = 200;
		var contract = RexPlusTestData.syncedContract();
		responseBody = "{\"referenceNumber\":\"" + contract.getRexContractNumber() + "\"}";

		RexSyncResult result = client().annulContract(contract, "No se presentó a trabajar");

		assertThat(result.success()).isTrue();
		assertThat(result.externalReferenceNumber()).isEqualTo(contract.getRexContractNumber());
		assertThat(lastPath).isEqualTo("/api/v1/contracts/" + contract.getRexContractNumber() + "/annulment");
		assertThat(lastBody).contains("\"reason\":\"No se presentó a trabajar\"");
	}

	@Test
	void annulContract_sinReferenciaEnRex_fallaSinLlamarAlServicio() {
		RexSyncResult result = client().annulContract(RexPlusTestData.contract(), "motivo");

		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).isNotBlank();
		assertThat(lastPath).isNull();
	}

	@Test
	void syncAnnex_respuestaExitosa_retornaLaReferenciaDelAnexo() {
		responseBody = "{\"referenceNumber\":\"REX-ANX-000001\"}";

		RexSyncResult result = client().syncAnnex(RexPlusTestData.annex());

		assertThat(result.success()).isTrue();
		assertThat(result.externalReferenceNumber()).isEqualTo("REX-ANX-000001");
		assertThat(lastPath).isEqualTo("/api/v1/annexes");
		assertThat(lastBody).contains("\"annexType\":\"SALARY\"");
	}

	@Test
	void syncContract_rexRechazaConError4xx_retornaElMensajeDeRex() {
		status = 422;
		responseBody = "{\"error\":{\"code\":\"UNPROCESSABLE\",\"message\":\"El contrato REX-000099 no existe en Rex+.\"}}";

		RexSyncResult result = client().syncContract(RexPlusTestData.contract());

		assertThat(lastPath).isEqualTo("/api/v1/contracts");
		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).isEqualTo("El contrato REX-000099 no existe en Rex+.");
	}

	@Test
	void syncContract_rexResponde503_retornaFalloGenericoSinExponerElMensajeDeRex() {
		status = 503;
		responseBody = "{\"error\":{\"code\":\"REX_UNAVAILABLE\",\"message\":\"Rex+ no respondió (falla simulada).\"}}";

		RexSyncResult result = client().syncContract(RexPlusTestData.contract());

		assertThat(lastPath).isEqualTo("/api/v1/contracts");
		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).isEqualTo(GENERIC_FAILURE);
	}

	@Test
	void syncContract_rexTardaMasQueElTimeoutDeLectura_retornaFalloSinLanzarExcepcion() {
		delayMs = SLOW_RESPONSE_MS;

		RexSyncResult result = clientWithReadTimeout(SHORT_READ_TIMEOUT_MS).syncContract(RexPlusTestData.contract());

		assertThat(lastPath).isEqualTo("/api/v1/contracts");
		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).isEqualTo(GENERIC_FAILURE);
	}

	@Test
	void syncContract_servicioApagado_retornaFalloSinLanzarExcepcion() {
		RexPlusHttpClient client = client();
		server.stop(0);

		RexSyncResult result = client.syncContract(RexPlusTestData.contract());

		assertThat(lastPath).isNull();
		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).isEqualTo(GENERIC_FAILURE);
	}

	@Test
	void syncContract_respuestaSinNumeroDeReferencia_retornaFallo() {
		responseBody = "{}";

		RexSyncResult result = client().syncContract(RexPlusTestData.contract());

		assertThat(lastPath).isEqualTo("/api/v1/contracts");
		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).isEqualTo("Rex+ respondió sin número de referencia.");
	}

	@Test
	void syncContract_respuestaQueNoEsJson_retornaFalloSinLanzarExcepcion() {
		responseBody = "esto no es json";

		RexSyncResult result = client().syncContract(RexPlusTestData.contract());

		assertThat(lastPath).isEqualTo("/api/v1/contracts");
		assertThat(result.success()).isFalse();
		assertThat(result.errorMessage()).isEqualTo(GENERIC_FAILURE);
	}

	@Test
	void config_sinApiKey_falla() {
		assertThatThrownBy(() -> RexPlusHttpConfig.create(RestClient.builder(), baseUrl(), " ", CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("rexplus.http.api-key");
	}

	@Test
	void config_noModificaElBuilderCompartido() {
		RestClient.Builder shared = RestClient.builder();

		RexPlusHttpConfig.create(shared, baseUrl(), API_KEY, CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
		shared.build().post().uri(baseUrl() + "/otro").retrieve().toBodilessEntity();

		assertThat(lastPath).isEqualTo("/otro");
		assertThat(lastApiKey).isNull();
	}

	private RexPlusHttpClient client() {
		return clientWithReadTimeout(READ_TIMEOUT_MS);
	}

	private RexPlusHttpClient clientWithReadTimeout(long readTimeoutMs) {
		RestClient restClient = RexPlusHttpConfig.create(RestClient.builder(), baseUrl(), API_KEY, CONNECT_TIMEOUT_MS, readTimeoutMs);
		return new RexPlusHttpClient(restClient);
	}

	private String baseUrl() {
		return "http://127.0.0.1:" + server.getAddress().getPort();
	}

	private void handle(HttpExchange exchange) throws IOException {
		lastPath = exchange.getRequestURI().getPath();
		lastApiKey = exchange.getRequestHeaders().getFirst("X-API-Key");
		lastBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		pause(delayMs);

		byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(bytes);
		}
	}

	private static void pause(long millis) {
		if (millis <= 0) {
			return;
		}
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
