package com.promaty.rrhh.services.rexplus;

import java.math.BigDecimal;

public record RexAnnexRequest(
	String externalId,
	String contractReferenceNumber,
	String annexType,
	String effectiveDate,
	BigDecimal newBaseSalary
) {
}
