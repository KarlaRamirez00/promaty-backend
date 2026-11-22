package com.promaty.rrhh.services.rexplus;

public record RexTerminationRequest(
	String externalId,
	String contractReferenceNumber,
	String terminationDate,
	String legalCause,
	boolean definitive
) {
}
