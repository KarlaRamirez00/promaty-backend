package com.promaty.rrhh.services.rexplus;

import java.time.LocalDate;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Termination;

final class RexPlusPayloadMapper {

	private RexPlusPayloadMapper() {
	}

	static RexContractRequest toRequest(Contract contract) {
		return new RexContractRequest(
			String.valueOf(contract.getId()),
			contract.getStaff().getIdentificationNumber(),
			contract.getContractType().getCode(),
			isoDate(contract.getStartDate()),
			isoDate(contract.getEndDate()),
			contract.getBaseSalary(),
			String.valueOf(contract.getProject().getId())
		);
	}

	static RexTerminationRequest toRequest(Termination termination) {
		return new RexTerminationRequest(
			String.valueOf(termination.getId()),
			termination.getContract().getRexContractNumber(),
			isoDate(termination.getTerminationDate()),
			termination.getLegalTerminationCause().getCode(),
			Boolean.TRUE.equals(termination.getDefinitiveTermination())
		);
	}

	static RexAnnexRequest toRequest(Annex annex) {
		return new RexAnnexRequest(
			String.valueOf(annex.getId()),
			annex.getContract().getRexContractNumber(),
			annex.getAnnexType().getName(),
			isoDate(annex.getEffectiveDate()),
			annex.getNewBaseSalary()
		);
	}

	private static String isoDate(LocalDate date) {
		return date == null ? null : date.toString();
	}
}
