package com.promaty.rrhh.services.rexplus;

import java.math.BigDecimal;

public record RexContractRequest(
	String externalId,
	String staffRut,
	String contractType,
	String startDate,
	String endDate,
	BigDecimal baseSalary,
	String projectCode
) {
}
