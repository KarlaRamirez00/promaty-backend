package com.promaty.rrhh.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetrySyncResultDto {

	private boolean synced;
	private String syncErrorMessage;
}
