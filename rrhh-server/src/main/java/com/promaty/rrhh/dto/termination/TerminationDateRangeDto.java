package com.promaty.rrhh.dto.termination;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TerminationDateRangeDto {

	private LocalDate minTerminationDate;
	private LocalDate maxTerminationDate;
}
