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
	void annulContract_sinForzarError_retornaExitoConElMismoNumeroDeReferencia() {
		RexPlusStubClient client = new RexPlusStubClient(false);
		Contract contract = new Contract();
		contract.setRexContractNumber("REX-123");

		RexSyncResult resultado = client.annulContract(contract, "No se presentó a trabajar");

		assertThat(resultado.success()).isTrue();
		assertThat(resultado.externalReferenceNumber()).isEqualTo("REX-123");
	}

	@Test
	void annulContract_forzandoError_retornaFallo() {
		RexPlusStubClient client = new RexPlusStubClient(true);

		RexSyncResult resultado = client.annulContract(new Contract(), "motivo");

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

	@Test
	void annulTermination_sinForzarError_retornaExitoConElMismoNumeroDeReferencia() {
		RexPlusStubClient client = new RexPlusStubClient(false);
		Termination termination = new Termination();
		termination.setRexTerminationNumber("REX-TERM-123");

		RexSyncResult resultado = client.annulTermination(termination, "Error de digitación");

		assertThat(resultado.success()).isTrue();
		assertThat(resultado.externalReferenceNumber()).isEqualTo("REX-TERM-123");
	}

	@Test
	void annulTermination_forzandoError_retornaFallo() {
		RexPlusStubClient client = new RexPlusStubClient(true);

		RexSyncResult resultado = client.annulTermination(new Termination(), "Error de digitación");

		assertThat(resultado.success()).isFalse();
		assertThat(resultado.errorMessage()).isNotBlank();
	}
}
