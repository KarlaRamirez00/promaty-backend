package com.promaty.rrhh.dto.projecttype;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTypeDetailDto {

	private Long id;
	private String name;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
