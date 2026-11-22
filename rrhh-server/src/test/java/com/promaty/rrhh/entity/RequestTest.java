package com.promaty.rrhh.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RequestTest {

	@Test
	void recordSyncFailure_guardaElMotivoYClearSyncFailureLoBorra() {
		Request request = new Request();

		request.recordSyncFailure("Rex+ no respondió.");
		assertThat(request.getSyncErrorMessage()).isEqualTo("Rex+ no respondió.");

		request.clearSyncFailure();
		assertThat(request.getSyncErrorMessage()).isNull();
	}

	@Test
	void recordSyncFailure_conMotivoMasLargoQueLaColumna_loRecorta() {
		Request request = new Request();

		request.recordSyncFailure("x".repeat(Request.SYNC_ERROR_MESSAGE_MAX_LENGTH + 20));

		assertThat(request.getSyncErrorMessage()).hasSize(Request.SYNC_ERROR_MESSAGE_MAX_LENGTH);
	}
}
