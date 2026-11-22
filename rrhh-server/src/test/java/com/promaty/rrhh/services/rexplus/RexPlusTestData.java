package com.promaty.rrhh.services.rexplus;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.test.util.ReflectionTestUtils;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexType;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.entity.Termination;

final class RexPlusTestData {

	private RexPlusTestData() {
	}

	static Contract contract() {
		Staff staff = new Staff();
		staff.setIdentificationNumber("12.345.678-9");
		ContractType contractType = new ContractType();
		contractType.setCode("O");
		Project project = new Project();
		withId(project, 7L);

		Contract contract = new Contract();
		withId(contract, 10L);
		contract.setStaff(staff);
		contract.setContractType(contractType);
		contract.setProject(project);
		contract.setStartDate(LocalDate.of(2026, 12, 1));
		contract.setBaseSalary(new BigDecimal("850000"));
		return contract;
	}

	static Contract syncedContract() {
		Contract contract = contract();
		contract.setRexContractNumber("REX-000001");
		return contract;
	}

	static Termination termination() {
		LegalTerminationCause cause = new LegalTerminationCause();
		cause.setCode("ART_159_4");
		Termination termination = new Termination();
		withId(termination, 45L);
		termination.setContract(syncedContract());
		termination.setTerminationDate(LocalDate.of(2027, 1, 15));
		termination.setLegalTerminationCause(cause);
		termination.setDefinitiveTermination(true);
		return termination;
	}

	static Annex annex() {
		AnnexType annexType = new AnnexType();
		annexType.setName("SALARY");
		Annex annex = new Annex();
		withId(annex, 7L);
		annex.setContract(syncedContract());
		annex.setAnnexType(annexType);
		annex.setEffectiveDate(LocalDate.of(2027, 2, 1));
		annex.setNewBaseSalary(new BigDecimal("900000"));
		return annex;
	}

	private static void withId(Object entity, Long id) {
		ReflectionTestUtils.setField(entity, "id", id);
	}
}
