package com.promaty.rrhh.services.rexplus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Termination;

class RexPlusStubClientTest {

	@Test
	void syncContract_sinForzarError_retornaExito() {
		RexPlusStubClient client = new RexPlusStubClient(false);

		RexSyncResult resultado = client.syncContract(new Contract());

		assertThat(resultado.success()).isTrue();
		assertThat(resultado.externalReferenceNumber()).startsWith("REX-");
	}

	@Test
	void syncContract_forzandoError_retornaFallo() {
		RexPlusStubClient client = new RexPlusStubClient(true);

		RexSyncResult resultado = client.syncContract(new Contract());

		assertThat(resultado.success()).isFalse();
		assertThat(resultado.errorMessage()).isNotBlank();
	}

	@Test
	void syncTermination_sinForzarError_retornaExito() {
		RexPlusStubClient client = new RexPlusStubClient(false);

		RexSyncResult resultado = client.syncTermination(new Termination());

		assertThat(resultado.success()).isTrue();
		assertThat(resultado.externalReferenceNumber()).startsWith("REX-TERM-");
	}

	@Test
	void syncTermination_forzandoError_retornaFallo() {
		RexPlusStubClient client = new RexPlusStubClient(true);

		RexSyncResult resultado = client.syncTermination(new Termination());

		assertThat(resultado.success()).isFalse();
		assertThat(resultado.errorMessage()).isNotBlank();
	}
}
