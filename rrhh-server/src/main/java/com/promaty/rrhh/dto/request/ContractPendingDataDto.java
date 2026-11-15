package com.promaty.rrhh.dto.request;

import com.promaty.rrhh.dto.contract.ContractFieldsDto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContractPendingDataDto extends ContractFieldsDto {

	@NotNull(message = "El colaborador es obligatorio.")
	private Long staffId;
}
