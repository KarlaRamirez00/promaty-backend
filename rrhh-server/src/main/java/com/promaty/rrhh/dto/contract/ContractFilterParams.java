package com.promaty.rrhh.dto.contract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractFilterParams {

	private String search;
	private Long projectId;
	private Long statusId;
}
