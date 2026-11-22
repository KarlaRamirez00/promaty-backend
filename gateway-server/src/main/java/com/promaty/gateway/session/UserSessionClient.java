package com.promaty.gateway.session;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.promaty.contracts.auth.InternalApiHeader;
import com.promaty.contracts.auth.UserSessionStatusDto;
import com.promaty.gateway.dto.response.BaseData;

@Component
public class UserSessionClient {

	private static final String USER_SERVICE_ID = "user-server";
	private static final String SESSION_PATH = "/internal/users/{id}/session";
	private static final ParameterizedTypeReference<BaseData<UserSessionStatusDto>> RESPONSE_TYPE =
		new ParameterizedTypeReference<>() {
		};

	private final LoadBalancerClient loadBalancerClient;
	private final RestClient restClient;

	public UserSessionClient(
		LoadBalancerClient loadBalancerClient,
		@Value("${internal-api.secret}") String internalApiSecret,
		@Value("${session.lookup-timeout-ms}") long lookupTimeoutMs
	) {
		this.loadBalancerClient = loadBalancerClient;
		this.restClient = buildRestClient(internalApiSecret, Duration.ofMillis(lookupTimeoutMs));
	}

	public Optional<UserSessionStatusDto> fetch(String userId) {
		ServiceInstance instance = loadBalancerClient.choose(USER_SERVICE_ID);
		if (instance == null) {
			return Optional.empty();
		}
		try {
			BaseData<UserSessionStatusDto> body = restClient.get()
				.uri(instance.getUri() + SESSION_PATH, userId)
				.retrieve()
				.body(RESPONSE_TYPE);
			return Optional.ofNullable(body).map(BaseData::getData);
		} catch (RestClientException ex) {
			return Optional.empty();
		}
	}

	// Sin @LoadBalanced a propósito: un RestClient.Builder balanceado como bean lo tomaría también el proxy del gateway.
	private static RestClient buildRestClient(String internalApiSecret, Duration timeout) {
		HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(timeout)
			.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(timeout);
		return RestClient.builder()
			.defaultHeader(InternalApiHeader.SECRET, internalApiSecret)
			.requestFactory(requestFactory)
			.build();
	}
}
