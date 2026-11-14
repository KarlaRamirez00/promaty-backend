package com.promaty.rrhh.services.annex.business.change;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.Contract;

class SalaryIncreaseRuleTest {

	private static final BigDecimal SUELDO_ACTUAL = BigDecimal.valueOf(850000);

	private final SalaryIncreaseRule rule = new SalaryIncreaseRule();

	@Test
	void validate_sueldoMayorAlActual_noAgregaErrores() {
		assertThat(validar(BigDecimal.valueOf(900000))).isEmpty();
	}

	@Test
	void validate_sueldoIgualAlActual_rechazaPorqueDebeSerUnAumento() {
		assertThat(validar(SUELDO_ACTUAL)).containsEntry("newBaseSalary", "El nuevo sueldo base debe ser mayor al actual.");
	}

	@Test
	void validate_sinSueldo_loExigeComoObligatorio() {
		assertThat(validar(null)).containsEntry("newBaseSalary", "El nuevo sueldo base es obligatorio.");
	}

	@Test
	void apply_guardaElSueldoAnteriorYActualizaElContrato() {
		Contract contract = contratoConSueldo();
		Annex annex = new Annex();
		annex.setNewBaseSalary(BigDecimal.valueOf(900000));

		rule.apply(annex, contract);

		assertThat(annex.getPreviousBaseSalary()).isEqualTo(SUELDO_ACTUAL);
		assertThat(contract.getBaseSalary()).isEqualTo(BigDecimal.valueOf(900000));
	}

	private Map<String, String> validar(BigDecimal nuevoSueldo) {
		UpdateAnnexDto dto = new UpdateAnnexDto();
		dto.setNewBaseSalary(nuevoSueldo);
		Map<String, String> errores = new LinkedHashMap<>();
		rule.validate(dto, contratoConSueldo(), errores);
		return errores;
	}

	private Contract contratoConSueldo() {
		Contract contract = new Contract();
		contract.setBaseSalary(SUELDO_ACTUAL);
		return contract;
	}
}
