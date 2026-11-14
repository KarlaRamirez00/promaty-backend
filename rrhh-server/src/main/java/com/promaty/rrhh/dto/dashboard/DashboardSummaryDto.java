package com.promaty.rrhh.dto.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardSummaryDto {

	private PendingRequestsDto pendingRequests;
	private Long activeStaff;
	private Long blockedStaff;
	private Long expiringContracts;
	private Integer expiringContractsDays;
	private Long hiresThisMonth;
	private Long hiresNextMonth;
	private Long terminationsThisMonth;
}
