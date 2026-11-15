package com.promaty.rrhh.dto.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentRequirementDto {

	private String type;
	private String label;
	private Boolean required;
	private Integer maxFiles;
	private Boolean fulfilled;
}
