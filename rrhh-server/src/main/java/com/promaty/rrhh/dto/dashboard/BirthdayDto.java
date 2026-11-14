package com.promaty.rrhh.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BirthdayDto {

	private Long staffId;
	private String fullName;
	private Integer day;
	private Integer month;
	private String costCenterCode;
}
