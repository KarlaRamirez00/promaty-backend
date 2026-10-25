package com.promaty.rrhh.entity;

import java.util.Arrays;
import java.util.Optional;

public enum RequestRejectionReasonSubModule {

	CONTRACTS("contracts", "Contratos"),
	TERMINATIONS("terminations", "Finiquitos");

	private final String code;
	private final String label;

	RequestRejectionReasonSubModule(String code, String label) {
		this.code = code;
		this.label = label;
	}

	public String getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public static Optional<RequestRejectionReasonSubModule> fromCode(String code) {
		return Arrays.stream(values()).filter(valor -> valor.code.equals(code)).findFirst();
	}
}
