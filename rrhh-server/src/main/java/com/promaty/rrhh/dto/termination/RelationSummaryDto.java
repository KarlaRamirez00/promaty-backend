package com.promaty.rrhh.dto.termination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vista mínima {id, name} de una FK de Termination (legalTerminationCause, nonRehireReason) para el
 * DTO de detalle — mismo patrón que dto/contract.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelationSummaryDto {

	private Long id;
	private String name;
}
