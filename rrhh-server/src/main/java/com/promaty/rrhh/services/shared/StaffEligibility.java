package com.promaty.rrhh.services.shared;

import java.util.LinkedHashMap;
import java.util.Map;

import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.BusinessValidationException;

public final class StaffEligibility {

	private static final String VALIDATION_MESSAGE = "La validacion fallo para uno o mas campos.";
	private static final String FIELD_STAFF = "staffId";
	private static final String BLOCKED_FOR_HIRING = "El colaborador está bloqueado y no puede ser contratado.";
	private static final String BLOCKED_FOR_ANNEX = "El colaborador está bloqueado y no puede recibir anexos.";

	private StaffEligibility() {
	}

	public static void collectHiringErrors(Staff staff, Map<String, String> errors) {
		collectBlockedError(staff, BLOCKED_FOR_HIRING, errors);
	}

	public static void validateHireable(Staff staff) {
		throwIfBlocked(staff, BLOCKED_FOR_HIRING);
	}

	public static void collectAnnexErrors(Staff staff, Map<String, String> errors) {
		collectBlockedError(staff, BLOCKED_FOR_ANNEX, errors);
	}

	private static void collectBlockedError(Staff staff, String message, Map<String, String> errors) {
		if (staff.isBlocked()) {
			errors.put(FIELD_STAFF, message);
		}
	}

	private static void throwIfBlocked(Staff staff, String message) {
		Map<String, String> errors = new LinkedHashMap<>();
		collectBlockedError(staff, message, errors);
		if (!errors.isEmpty()) {
			throw new BusinessValidationException(VALIDATION_MESSAGE, errors);
		}
	}
}
