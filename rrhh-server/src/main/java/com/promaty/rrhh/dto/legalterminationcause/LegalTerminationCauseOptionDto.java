package com.promaty.rrhh.dto.legalterminationcause;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LegalTerminationCauseOptionDto {

	private Long id;
	private String name;
	private String code;
	private Boolean requiresAttachment;
	private String attachmentLabel;
}
