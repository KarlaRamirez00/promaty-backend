package com.promaty.rrhh.dto.contract;

import java.util.List;

import com.promaty.rrhh.dto.file.AttachmentItemDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateContractAttachmentsDto {

	@Valid
	@NotNull(message = "La lista de adjuntos es obligatoria.")
	private List<AttachmentItemDto> attachments;
}
