package com.promaty.rrhh.services.annex.business.validation;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.dto.request.AnnexPendingDataDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexChangeValues;
import com.promaty.rrhh.entity.AnnexType;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.AnnexTypeRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.annex.business.change.AnnexChangeField;
import com.promaty.rrhh.services.annex.business.change.AnnexChangeRule;
import com.promaty.rrhh.services.annex.business.change.AnnexChangeRuleRegistry;
import com.promaty.rrhh.services.shared.StaffEligibility;

@Component
public class AnnexValidation {

	private static final String VALIDATION_MESSAGE = "La validacion fallo para uno o mas campos.";
	private static final String FIELD_STAFF = "staffId";
	private static final String FIELD_ANNEX_TYPE = "annexTypeId";
	private static final String FIELD_EFFECTIVE_DATE = "effectiveDate";
	private static final String FIELD_STATUS = "status";
	private static final String CODE_ACTIVE = "ACTIVE";
	private static final String CODE_PENDING_APPROVAL = "PENDING_APPROVAL";
	private static final List<String> OPEN_STATUS_CODES = List.of(CODE_PENDING_APPROVAL, "PENDING_VALIDATION");
	private static final String NOT_APPLICABLE = "No aplica al tipo de anexo seleccionado.";

	private final StaffRepository staffRepository;
	private final ContractRepository contractRepository;
	private final AnnexTypeRepository annexTypeRepository;
	private final AnnexRepository annexRepository;
	private final TerminationRepository terminationRepository;
	private final AnnexChangeRuleRegistry ruleRegistry;

	public AnnexValidation(
		StaffRepository staffRepository,
		ContractRepository contractRepository,
		AnnexTypeRepository annexTypeRepository,
		AnnexRepository annexRepository,
		TerminationRepository terminationRepository,
		AnnexChangeRuleRegistry ruleRegistry
	) {
		this.staffRepository = staffRepository;
		this.contractRepository = contractRepository;
		this.annexTypeRepository = annexTypeRepository;
		this.annexRepository = annexRepository;
		this.terminationRepository = terminationRepository;
		this.ruleRegistry = ruleRegistry;
	}

	public void collectCreateErrors(AnnexPendingDataDto dto, Map<String, String> errors) {
		Optional<Staff> staff = staffRepository.findById(dto.getStaffId());
		if (staff.isEmpty()) {
			errors.put(FIELD_STAFF, "El colaborador indicado no existe.");
			return;
		}
		StaffEligibility.collectAnnexErrors(staff.get(), errors);
		Optional<Contract> activeContract = contractRepository.findByStaffIdAndStatus_Code(dto.getStaffId(), CODE_ACTIVE);
		if (activeContract.isEmpty()) {
			errors.put(FIELD_STAFF, "El colaborador no tiene un contrato activo.");
			return;
		}
		Contract contract = activeContract.get();
		collectContractConflicts(contract, errors);
		collectEffectiveDateErrors(dto.getEffectiveDate(), contract, errors);
		resolveActiveType(dto.getAnnexTypeId(), errors)
			.ifPresent(type -> collectChangeErrors(type.getCode(), dto, contract, errors));
	}

	public void validateUpdate(Annex annex, UpdateAnnexDto dto) {
		Map<String, String> errors = new LinkedHashMap<>();
		StaffEligibility.collectAnnexErrors(annex.getStaff(), errors);
		collectEffectiveDateErrors(dto.getEffectiveDate(), annex.getContract(), errors);
		resolveTypeForUpdate(annex, dto.getAnnexTypeId(), errors)
			.ifPresent(typeCode -> collectChangeErrors(typeCode, dto, annex.getContract(), errors));
		throwIfErrors(errors);
	}

	private Optional<AnnexTypeCode> resolveTypeForUpdate(Annex annex, Long annexTypeId, Map<String, String> errors) {
		if (annexTypeId == null || annexTypeId.equals(annex.getAnnexType().getId())) {
			return Optional.of(annex.getTypeCode());
		}
		return resolveActiveType(annexTypeId, errors).map(AnnexType::getCode);
	}

	public void validateApproval(Annex annex) {
		Map<String, String> errors = new LinkedHashMap<>();
		StaffEligibility.collectAnnexErrors(annex.getStaff(), errors);
		Contract contract = annex.getContract();
		if (CODE_ACTIVE.equals(contract.getStatus().getCode())) {
			collectChangeErrors(annex.getTypeCode(), annex, contract, errors);
		} else {
			errors.put(FIELD_STATUS, "El contrato del anexo ya no está activo.");
		}
		throwIfErrors(errors);
	}

	private void collectContractConflicts(Contract contract, Map<String, String> errors) {
		if (annexRepository.existsByContract_IdAndStatus_CodeIn(contract.getId(), OPEN_STATUS_CODES)) {
			errors.putIfAbsent(FIELD_STAFF, "Ya existe un anexo en curso para el contrato de este colaborador.");
		}
		if (!terminationRepository.findByContract_IdAndStatus_CodeIn(contract.getId(), OPEN_STATUS_CODES).isEmpty()) {
			errors.putIfAbsent(FIELD_STAFF, "El contrato tiene un finiquito en curso: no admite anexos.");
		}
	}

	private void collectEffectiveDateErrors(LocalDate effectiveDate, Contract contract, Map<String, String> errors) {
		YearMonth currentMonth = YearMonth.now();
		LocalDate windowStart = currentMonth.atDay(1);
		LocalDate windowEnd = currentMonth.plusMonths(1).atEndOfMonth();
		if (effectiveDate.isBefore(windowStart) || effectiveDate.isAfter(windowEnd)) {
			errors.put(FIELD_EFFECTIVE_DATE, "La fecha de vigencia debe estar dentro del mes actual o el siguiente.");
		} else if (effectiveDate.isBefore(contract.getStartDate())) {
			errors.put(FIELD_EFFECTIVE_DATE, "La fecha de vigencia no puede ser anterior al inicio del contrato.");
		} else if (contract.getEndDate() != null && effectiveDate.isAfter(contract.getEndDate())) {
			errors.put(FIELD_EFFECTIVE_DATE, "La fecha de vigencia no puede ser posterior al término del contrato.");
		}
	}

	private Optional<AnnexType> resolveActiveType(Long annexTypeId, Map<String, String> errors) {
		Optional<AnnexType> annexType = annexTypeRepository.findById(annexTypeId);
		if (annexType.isEmpty()) {
			errors.put(FIELD_ANNEX_TYPE, "El tipo de anexo indicado no existe.");
			return Optional.empty();
		}
		if (!Boolean.TRUE.equals(annexType.get().getActive())) {
			errors.put(FIELD_ANNEX_TYPE, "El tipo de anexo indicado no está activo.");
			return Optional.empty();
		}
		return annexType;
	}

	private void collectChangeErrors(AnnexTypeCode typeCode, AnnexChangeValues values, Contract contract, Map<String, String> errors) {
		AnnexChangeRule rule = ruleRegistry.get(typeCode);
		Arrays.stream(AnnexChangeField.values())
			.filter(field -> !rule.fields().contains(field) && field.isPresentIn(values))
			.forEach(field -> errors.put(field.getFieldName(), NOT_APPLICABLE));
		rule.validate(values, contract, errors);
	}

	private void throwIfErrors(Map<String, String> errors) {
		if (!errors.isEmpty()) {
			throw new BusinessValidationException(VALIDATION_MESSAGE, errors);
		}
	}
}
