package com.promaty.authorizer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.promaty.contracts.auth.InternalApiHeader;

import feign.RequestInterceptor;

// Sin @Configuration a proposito: solo la aplica el cliente Feign que la referencia, no todo el contexto.
public class InternalApiFeignConfig {

	@Bean
	public RequestInterceptor internalApiSecretInterceptor(@Value("${internal-api.secret}") String secret) {
		return template -> template.header(InternalApiHeader.SECRET, secret);
	}
}
