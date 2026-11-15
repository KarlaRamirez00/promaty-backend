package com.promaty.rrhh.dto.annex;

import java.util.List;

import com.promaty.rrhh.dto.file.AttachmentItemDto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAnnexDto extends AnnexChangeFieldsDto {

	private Long annexTypeId;

	@Valid
	private List<AttachmentItemDto> attachments;
}
