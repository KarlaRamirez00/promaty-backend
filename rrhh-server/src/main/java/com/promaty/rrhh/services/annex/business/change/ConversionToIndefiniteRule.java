package com.promaty.rrhh.services.annex.business.change;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexChangeValues;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractTypeRepository;

@Component
public class ConversionToIndefiniteRule implements AnnexChangeRule {

	private static final String FIELD_ANNEX_TYPE = "annexTypeId";
	private static final String CODE_INDEFINITE = "I";

	private final ContractTypeRepository contractTypeRepository;

	public ConversionToIndefiniteRule(ContractTypeRepository contractTypeRepository) {
		this.contractTypeRepository = contractTypeRepository;
	}

	@Override
	public AnnexTypeCode supports() {
		return AnnexTypeCode.CONVERSION_TO_INDEFINITE;
	}

	@Override
	public Set<AnnexChangeField> fields() {
		return Set.of();
	}

	@Override
	public void validate(AnnexChangeValues values, Contract contract, Map<String, String> errors) {
		if (CODE_INDEFINITE.equals(contract.getContractType().getCode())) {
			errors.put(FIELD_ANNEX_TYPE, "El contrato ya es indefinido.");
		}
	}

	@Override
	public void apply(Annex annex, Contract contract) {
		annex.setPreviousContractType(contract.getContractType());
		annex.setPreviousEndDate(contract.getEndDate());
		contract.setContractType(contractTypeRepository.findByCode(CODE_INDEFINITE)
			.orElseThrow(() -> new ResourceNotFoundException("El tipo de contrato indefinido no está sembrado.")));
		contract.setEndDate(null);
	}
}
