package com.promaty.rrhh.services.rexplus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.promaty.rrhh.config.RexPlusHttpConfig;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Termination;

@Component
@ConditionalOnProperty(name = "rexplus.mode", havingValue = "http")
public class RexPlusHttpClient implements RexPlusClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(RexPlusHttpClient.class);
	private static final String CONTRACTS_PATH = "/api/v1/contracts";
	private static final String TERMINATIONS_PATH = "/api/v1/terminations";
	private static final String ANNULMENT_PATH = "/api/v1/terminations/{reference}/annulment";
	private static final String ANNEXES_PATH = "/api/v1/annexes";
	private static final String MSG_NO_RESPONSE = "Rex+ no respondió.";
	private static final String MSG_NO_REFERENCE = "Rex+ respondió sin número de referencia.";
	private static final String MSG_TERMINATION_NOT_SYNCED = "La terminación no tiene número de referencia en Rex+.";

	private final RestClient restClient;

	public RexPlusHttpClient(@Qualifier(RexPlusHttpConfig.REST_CLIENT_BEAN) RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public RexSyncResult syncContract(Contract contract) {
		return post(CONTRACTS_PATH, RexPlusPayloadMapper.toRequest(contract));
	}

	@Override
	public RexSyncResult syncTermination(Termination termination) {
		return post(TERMINATIONS_PATH, RexPlusPayloadMapper.toRequest(termination));
	}

	@Override
	public RexSyncResult annulTermination(Termination termination, String reason) {
		String reference = termination.getRexTerminationNumber();
		if (reference == null || reference.isBlank()) {
			return RexSyncResult.failure(MSG_TERMINATION_NOT_SYNCED);
		}
		return post(ANNULMENT_PATH, new RexAnnulmentRequest(reason), reference);
	}

	@Override
	public RexSyncResult syncAnnex(Annex annex) {
		return post(ANNEXES_PATH, RexPlusPayloadMapper.toRequest(annex));
	}

	private RexSyncResult post(String uri, Object body, Object... uriVariables) {
		try {
			RexReferenceResponse response = restClient.post()
				.uri(uri, uriVariables)
				.contentType(MediaType.APPLICATION_JSON)
				.body(body)
				.retrieve()
				.body(RexReferenceResponse.class);
			return toResult(response);
		} catch (RestClientResponseException e) {
			LOGGER.warn("Rex+ respondió HTTP {} en {}.", e.getStatusCode().value(), uri);
			return RexSyncResult.failure(rejectionMessage(e));
		} catch (RestClientException e) {
			LOGGER.warn("No se pudo comunicar con Rex+ en {}.", uri, e);
			return RexSyncResult.failure(MSG_NO_RESPONSE);
		}
	}

	private RexSyncResult toResult(RexReferenceResponse response) {
		if (response == null || response.referenceNumber() == null || response.referenceNumber().isBlank()) {
			return RexSyncResult.failure(MSG_NO_REFERENCE);
		}
		return RexSyncResult.success(response.referenceNumber());
	}

	private String rejectionMessage(RestClientResponseException e) {
		if (!e.getStatusCode().is4xxClientError()) {
			return MSG_NO_RESPONSE;
		}
		try {
			RexErrorResponse error = e.getResponseBodyAs(RexErrorResponse.class);
			if (error != null && error.error() != null && error.error().message() != null) {
				return error.error().message();
			}
		} catch (RestClientException parseError) {
			LOGGER.debug("No se pudo leer el cuerpo de error de Rex+.", parseError);
		}
		return MSG_NO_RESPONSE;
	}
}
