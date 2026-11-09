package com.promaty.authorizer.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.promaty.contracts.auth.InternalApiHeader;

import feign.RequestInterceptor;
import feign.RequestTemplate;

class InternalApiFeignConfigTest {

	@Test
	void interceptor_agregaElSecretoDeServicioALaPeticion() {
		RequestInterceptor interceptor = new InternalApiFeignConfig().internalApiSecretInterceptor("secreto-de-prueba");
		RequestTemplate template = new RequestTemplate();

		interceptor.apply(template);

		assertThat(template.headers().get(InternalApiHeader.SECRET)).containsExactly("secreto-de-prueba");
	}
}
