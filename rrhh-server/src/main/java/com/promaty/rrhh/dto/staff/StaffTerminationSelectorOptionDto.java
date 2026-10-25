package com.promaty.rrhh.dto.staff;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StaffTerminationSelectorOptionDto {

	private Long id;
	private String name;
	private String identificationNumber;
	private LocalDate minTerminationDate;
	private LocalDate maxTerminationDate;
}
