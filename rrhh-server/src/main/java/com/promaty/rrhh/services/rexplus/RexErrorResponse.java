package com.promaty.rrhh.services.rexplus;

import java.util.Map;

public record RexErrorResponse(Detail error) {

	public record Detail(String code, String message, Map<String, String> fields) {
	}
}
