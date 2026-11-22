package com.promaty.rrhh.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty(name = "rexplus.mode", havingValue = "http")
public class RexPlusHttpConfig {

	public static final String REST_CLIENT_BEAN = "rexPlusRestClient";

	private static final String API_KEY_HEADER = "X-API-Key";

	@Bean(REST_CLIENT_BEAN)
	public RestClient rexPlusRestClient(
		RestClient.Builder restClientBuilder,
		@Value("${rexplus.http.base-url}") String baseUrl,
		@Value("${rexplus.http.api-key}") String apiKey,
		@Value("${rexplus.http.connect-timeout-ms:2000}") long connectTimeoutMs,
		@Value("${rexplus.http.read-timeout-ms:5000}") long readTimeoutMs
	) {
		return create(restClientBuilder, baseUrl, apiKey, connectTimeoutMs, readTimeoutMs);
	}

	public static RestClient create(
		RestClient.Builder restClientBuilder, String baseUrl, String apiKey, long connectTimeoutMs, long readTimeoutMs
	) {
		requireText(baseUrl, "rexplus.http.base-url");
		requireText(apiKey, "rexplus.http.api-key");

		// HTTP/1.1 explícito: el cliente JDK intenta h2c por defecto y algunos servidores no lo toleran.
		HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofMillis(connectTimeoutMs))
			.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

		// clone(): el builder es un bean compartido con NagerHolidayProvider y no debe heredar esta configuración.
		return restClientBuilder.clone()
			.baseUrl(baseUrl)
			.defaultHeader(API_KEY_HEADER, apiKey)
			.requestFactory(requestFactory)
			.build();
	}

	private static void requireText(String value, String property) {
		if (value == null || value.isBlank()) {
			throw new IllegalStateException("Con rexplus.mode=http la propiedad " + property + " es obligatoria.");
		}
	}
}
