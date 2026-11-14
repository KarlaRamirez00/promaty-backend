package com.promaty.rrhh.dto.request;

import com.promaty.rrhh.dto.annex.AnnexChangeFieldsDto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnexPendingDataDto extends AnnexChangeFieldsDto {

	@NotNull(message = "El colaborador es obligatorio.")
	private Long staffId;

	@NotNull(message = "El tipo de anexo es obligatorio.")
	private Long annexTypeId;
}
