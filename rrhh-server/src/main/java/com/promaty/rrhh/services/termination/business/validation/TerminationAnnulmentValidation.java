package com.promaty.rrhh.services.termination.business.validation;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.services.holidayprovider.BusinessDayCalculator;

@Component
public class TerminationAnnulmentValidation {

	private static final String VALIDATION_MESSAGE = "La validacion fallo para uno o mas campos.";
	private static final String CODE_APPROVED = "APPROVED";
	private static final long MAX_BUSINESS_DAYS = 5;

	private final BusinessDayCalculator businessDayCalculator;

	public TerminationAnnulmentValidation(BusinessDayCalculator businessDayCalculator) {
		this.businessDayCalculator = businessDayCalculator;
	}

	public void validateAnnul(Termination termination) {
		Map<String, String> errors = new LinkedHashMap<>();

		if (!CODE_APPROVED.equals(termination.getStatus().getCode())) {
			errors.put("status", "Solo un finiquito aprobado puede ser anulado.");
			throwIfErrors(errors);
			return;
		}

		LocalDate approvalDate = termination.getUpdatedAt().toLocalDate();
		long businessDaysElapsed = businessDayCalculator.countBusinessDaysBetween(approvalDate, LocalDate.now());
		if (businessDaysElapsed > MAX_BUSINESS_DAYS) {
			errors.put("status", "El plazo de " + MAX_BUSINESS_DAYS + " días hábiles para anular este finiquito ya venció.");
		}

		throwIfErrors(errors);
	}

	private void throwIfErrors(Map<String, String> errors) {
		if (!errors.isEmpty()) {
			throw new BusinessValidationException(VALIDATION_MESSAGE, errors);
		}
	}
}
