package com.promaty.rrhh.services.rexplus;

public record RexSyncResult(boolean success, String externalReferenceNumber, String errorMessage) {

	public static RexSyncResult success(String externalReferenceNumber) {
		return new RexSyncResult(true, externalReferenceNumber, null);
	}

	public static RexSyncResult failure(String errorMessage) {
		return new RexSyncResult(false, null, errorMessage);
	}
}
