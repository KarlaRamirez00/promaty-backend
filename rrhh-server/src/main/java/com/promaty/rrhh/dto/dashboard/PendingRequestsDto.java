package com.promaty.rrhh.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingRequestsDto {

	private Long contract;
	private Long termination;
	private Long annex;
	private PendingByTypeDto toApprove;
	private PendingByTypeDto toValidate;
}
