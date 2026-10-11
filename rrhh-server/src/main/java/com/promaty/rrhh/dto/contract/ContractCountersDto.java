package com.promaty.rrhh.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContractCountersDto {

	private long expired;
	private long expiringSoon;
}
