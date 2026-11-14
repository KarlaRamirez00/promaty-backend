package com.promaty.rrhh.services.annex.business.change;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexChangeValues;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.Contract;

@Component
public class SalaryIncreaseRule implements AnnexChangeRule {

	private static final String FIELD_NEW_BASE_SALARY = "newBaseSalary";

	@Override
	public AnnexTypeCode supports() {
		return AnnexTypeCode.SALARY_INCREASE;
	}

	@Override
	public Set<AnnexChangeField> fields() {
		return Set.of(AnnexChangeField.NEW_BASE_SALARY);
	}

	@Override
	public void validate(AnnexChangeValues values, Contract contract, Map<String, String> errors) {
		BigDecimal newBaseSalary = values.getNewBaseSalary();
		if (newBaseSalary == null) {
			errors.put(FIELD_NEW_BASE_SALARY, "El nuevo sueldo base es obligatorio.");
		} else if (newBaseSalary.compareTo(contract.getBaseSalary()) <= 0) {
			errors.put(FIELD_NEW_BASE_SALARY, "El nuevo sueldo base debe ser mayor al actual.");
		}
	}

	@Override
	public void apply(Annex annex, Contract contract) {
		annex.setPreviousBaseSalary(contract.getBaseSalary());
		contract.setBaseSalary(annex.getNewBaseSalary());
	}
}
