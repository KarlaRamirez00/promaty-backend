package com.promaty.rrhh.services.shared;

import java.util.Set;

public final class SyncRetryAction {

	public static final String CODE_SYNC_ERROR = "SYNC_ERROR";

	private SyncRetryAction() {
	}

	public static boolean isAvailable(String entityStatusCode, Set<String> authorities, String validatePermission) {
		return CODE_SYNC_ERROR.equals(entityStatusCode) && authorities.contains(validatePermission);
	}
}
