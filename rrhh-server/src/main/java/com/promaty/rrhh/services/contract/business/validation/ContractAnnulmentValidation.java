package com.promaty.rrhh.services.contract.business.validation;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.holidayprovider.BusinessDayCalculator;

@Component
public class ContractAnnulmentValidation {

	private static final String VALIDATION_MESSAGE = "La validación falló para uno o más campos.";
	private static final long MAX_BUSINESS_DAYS = 5;
	private static final List<String> OPEN_OR_APPROVED_CODES = List.of("PENDING_APPROVAL", "PENDING_VALIDATION", "APPROVED");
	private static final String FIELD_STATUS = "status";
	private static final String FIELD_TERMINATIONS = "terminations";
	private static final String FIELD_ANNEXES = "annexes";

	private final BusinessDayCalculator businessDayCalculator;
	private final TerminationRepository terminationRepository;
	private final AnnexRepository annexRepository;

	public ContractAnnulmentValidation(
		BusinessDayCalculator businessDayCalculator,
		TerminationRepository terminationRepository,
		AnnexRepository annexRepository
	) {
		this.businessDayCalculator = businessDayCalculator;
		this.terminationRepository = terminationRepository;
		this.annexRepository = annexRepository;
	}

	public void validateAnnul(Contract contract) {
		Map<String, String> errors = new LinkedHashMap<>();

		if (!Contract.STATUS_CODE_ACTIVE.equals(contract.getStatus().getCode())) {
			errors.put(FIELD_STATUS, "Solo un contrato aprobado y vigente puede ser anulado.");
			throw new BusinessValidationException(VALIDATION_MESSAGE, errors);
		}

		LocalDate approvalDate = contract.getUpdatedAt().toLocalDate();
		long businessDaysElapsed = businessDayCalculator.countBusinessDaysBetween(approvalDate, LocalDate.now());
		if (businessDaysElapsed > MAX_BUSINESS_DAYS) {
			errors.put(FIELD_STATUS, "El plazo de " + MAX_BUSINESS_DAYS + " días hábiles para anular este contrato ya venció.");
		}
		if (terminationRepository.existsByContract_IdAndStatus_CodeIn(contract.getId(), OPEN_OR_APPROVED_CODES)) {
			errors.put(FIELD_TERMINATIONS, "El contrato tiene un finiquito en curso o aprobado: resuélvelo antes de anular el contrato.");
		}
		if (annexRepository.existsByContract_IdAndStatus_CodeIn(contract.getId(), OPEN_OR_APPROVED_CODES)) {
			errors.put(FIELD_ANNEXES, "El contrato tiene un anexo en curso o aprobado: resuélvelo antes de anular el contrato.");
		}

		if (!errors.isEmpty()) {
			throw new BusinessValidationException(VALIDATION_MESSAGE, errors);
		}
	}
}
