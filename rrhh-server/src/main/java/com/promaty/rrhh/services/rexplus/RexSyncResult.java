package com.promaty.rrhh.services.rexplus;

public record RexSyncResult(boolean success, String externalContractNumber, String errorMessage) {

	public static RexSyncResult success(String externalContractNumber) {
		return new RexSyncResult(true, externalContractNumber, null);
	}

	public static RexSyncResult failure(String errorMessage) {
		return new RexSyncResult(false, null, errorMessage);
	}
}
