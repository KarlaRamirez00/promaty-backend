package com.promaty.rrhh.services.annex.business.change;

import java.util.Map;
import java.util.Set;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexChangeValues;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.Contract;

public interface AnnexChangeRule {

	AnnexTypeCode supports();

	Set<AnnexChangeField> fields();

	void validate(AnnexChangeValues values, Contract contract, Map<String, String> errors);

	void apply(Annex annex, Contract contract);
}
