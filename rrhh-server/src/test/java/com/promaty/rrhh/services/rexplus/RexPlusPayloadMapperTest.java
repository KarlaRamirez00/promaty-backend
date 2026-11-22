package com.promaty.rrhh.services.rexplus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Termination;

class RexPlusPayloadMapperTest {

	@Test
	void toRequest_contrato_mapeaCamposYFechasIso() {
		RexContractRequest request = RexPlusPayloadMapper.toRequest(RexPlusTestData.contract());

		assertThat(request.externalId()).isEqualTo("10");
		assertThat(request.staffRut()).isEqualTo("12.345.678-9");
		assertThat(request.contractType()).isEqualTo("O");
		assertThat(request.startDate()).isEqualTo("2026-12-01");
		assertThat(request.baseSalary()).isEqualByComparingTo("850000");
		assertThat(request.projectCode()).isEqualTo("7");
	}

	@Test
	void toRequest_contratoSinFechaDeTermino_enviaEndDateNulo() {
		Contract contract = RexPlusTestData.contract();

		assertThat(RexPlusPayloadMapper.toRequest(contract).endDate()).isNull();
	}

	@Test
	void toRequest_terminacion_usaElNumeroRexDelContrato() {
		RexTerminationRequest request = RexPlusPayloadMapper.toRequest(RexPlusTestData.termination());

		assertThat(request.externalId()).isEqualTo("45");
		assertThat(request.contractReferenceNumber()).isEqualTo("REX-000001");
		assertThat(request.terminationDate()).isEqualTo("2027-01-15");
		assertThat(request.legalCause()).isEqualTo("ART_159_4");
		assertThat(request.definitive()).isTrue();
	}

	@Test
	void toRequest_terminacionSinIndicadorDefinitivo_enviaFalso() {
		Termination termination = RexPlusTestData.termination();
		termination.setDefinitiveTermination(null);

		assertThat(RexPlusPayloadMapper.toRequest(termination).definitive()).isFalse();
	}

	@Test
	void toRequest_anexo_mapeaTipoFechaYSueldoNuevo() {
		RexAnnexRequest request = RexPlusPayloadMapper.toRequest(RexPlusTestData.annex());

		assertThat(request.externalId()).isEqualTo("7");
		assertThat(request.contractReferenceNumber()).isEqualTo("REX-000001");
		assertThat(request.annexType()).isEqualTo("SALARY");
		assertThat(request.effectiveDate()).isEqualTo("2027-02-01");
		assertThat(request.newBaseSalary()).isEqualByComparingTo("900000");
	}
}
