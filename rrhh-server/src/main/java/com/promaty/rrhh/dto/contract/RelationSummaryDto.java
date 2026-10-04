package com.promaty.rrhh.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vista mínima {id, name} de una FK de Contract (company, contractType, jobTitle, site, mealType,
 * transportType) para el DTO de detalle — mismo patrón que dto/project y dto/staff.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelationSummaryDto {

	private Long id;
	private String name;
}
