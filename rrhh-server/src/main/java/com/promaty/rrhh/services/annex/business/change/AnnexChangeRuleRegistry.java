package com.promaty.rrhh.services.annex.business.change;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.AnnexTypeCode;

@Component
public class AnnexChangeRuleRegistry {

	private final Map<AnnexTypeCode, AnnexChangeRule> rules;

	public AnnexChangeRuleRegistry(List<AnnexChangeRule> rules) {
		this.rules = rules.stream().collect(Collectors.toMap(AnnexChangeRule::supports, Function.identity()));
	}

	public AnnexChangeRule get(AnnexTypeCode code) {
		AnnexChangeRule rule = rules.get(code);
		if (rule == null) {
			throw new IllegalStateException("No hay una regla registrada para el tipo de anexo " + code + ".");
		}
		return rule;
	}
}
