package com.promaty.rrhh.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RequestCountersDto {

	private long pendingApproval;
	private long pendingValidation;
}
