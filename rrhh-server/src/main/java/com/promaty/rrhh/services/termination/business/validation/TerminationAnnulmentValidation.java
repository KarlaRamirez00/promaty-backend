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

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";
	private static final String CODE_APPROVED = "APPROVED";
	private static final long PLAZO_MAXIMO_DIAS_HABILES = 5;

	private final BusinessDayCalculator businessDayCalculator;

	public TerminationAnnulmentValidation(BusinessDayCalculator businessDayCalculator) {
		this.businessDayCalculator = businessDayCalculator;
	}

	public void validateAnnul(Termination termination) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (!CODE_APPROVED.equals(termination.getStatus().getCode())) {
			errores.put("status", "Solo un finiquito aprobado puede ser anulado.");
			lanzarSiHayErrores(errores);
			return;
		}

		LocalDate fechaAprobacion = termination.getUpdatedAt().toLocalDate();
		long diasHabilesTranscurridos = businessDayCalculator.countBusinessDaysBetween(fechaAprobacion, LocalDate.now());
		if (diasHabilesTranscurridos > PLAZO_MAXIMO_DIAS_HABILES) {
			errores.put("status", "El plazo de " + PLAZO_MAXIMO_DIAS_HABILES + " días hábiles para anular este finiquito ya venció.");
		}

		lanzarSiHayErrores(errores);
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
