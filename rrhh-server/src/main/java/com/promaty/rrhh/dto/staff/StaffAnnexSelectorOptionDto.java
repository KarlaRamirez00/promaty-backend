package com.promaty.rrhh.dto.staff;

import com.promaty.rrhh.dto.contract.ContractSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StaffAnnexSelectorOptionDto {

	private Long id;
	private String name;
	private String identificationNumber;
	private ContractSummaryDto activeContract;
}
