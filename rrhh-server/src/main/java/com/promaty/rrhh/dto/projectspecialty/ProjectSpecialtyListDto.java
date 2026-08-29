package com.promaty.rrhh.dto.projectspecialty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSpecialtyListDto {

	private Long id;
	private String name;
	private Boolean active;
}
