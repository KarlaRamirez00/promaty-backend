package com.promaty.rrhh.dto.contract;

import java.util.List;

import com.promaty.rrhh.dto.file.AttachmentItemDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateContractDto extends ContractFieldsDto {

	@NotNull(message = "El centro de costo es obligatorio.")
	private Long projectId;

	@Valid
	private List<AttachmentItemDto> attachments;
}
