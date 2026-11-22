package com.promaty.rrhh.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingByTypeDto {

	private Long contract;
	private Long termination;
	private Long annex;
}
