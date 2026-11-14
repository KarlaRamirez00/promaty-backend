package com.promaty.rrhh.services.annex.business.change;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexChangeValues;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.repository.AnnexRepository;

@Component
public class ContractExtensionRule implements AnnexChangeRule {

	private static final String FIELD_ANNEX_TYPE = "annexTypeId";
	private static final String FIELD_NEW_END_DATE = "newEndDate";
	private static final String CODE_FIXED_TERM = "F";
	private static final String CODE_APPROVED = "APPROVED";

	private final AnnexRepository annexRepository;

	public ContractExtensionRule(AnnexRepository annexRepository) {
		this.annexRepository = annexRepository;
	}

	@Override
	public AnnexTypeCode supports() {
		return AnnexTypeCode.CONTRACT_EXTENSION;
	}

	@Override
	public Set<AnnexChangeField> fields() {
		return Set.of(AnnexChangeField.NEW_END_DATE);
	}

	@Override
	public void validate(AnnexChangeValues values, Contract contract, Map<String, String> errors) {
		if (!CODE_FIXED_TERM.equals(contract.getContractType().getCode())) {
			errors.put(FIELD_ANNEX_TYPE, "La prórroga solo aplica a contratos a plazo fijo.");
			return;
		}
		if (hasApprovedExtension(contract)) {
			errors.put(
				FIELD_ANNEX_TYPE,
				"Este contrato ya tuvo una prórroga. Una segunda renovación lo convierte en indefinido: usa el anexo de paso a indefinido."
			);
		}
		LocalDate newEndDate = values.getNewEndDate();
		if (newEndDate == null) {
			errors.put(FIELD_NEW_END_DATE, "La nueva fecha de término es obligatoria.");
		} else if (!newEndDate.isAfter(contract.getEndDate())) {
			errors.put(FIELD_NEW_END_DATE, "La nueva fecha de término debe ser posterior al término actual del contrato.");
		}
	}

	@Override
	public void apply(Annex annex, Contract contract) {
		annex.setPreviousEndDate(contract.getEndDate());
		contract.setEndDate(annex.getNewEndDate());
	}

	private boolean hasApprovedExtension(Contract contract) {
		return annexRepository.existsByContract_IdAndAnnexType_CodeAndStatus_Code(
			contract.getId(), AnnexTypeCode.CONTRACT_EXTENSION, CODE_APPROVED
		);
	}
}
