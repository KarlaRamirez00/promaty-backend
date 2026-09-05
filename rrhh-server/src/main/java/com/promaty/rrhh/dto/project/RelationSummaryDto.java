package com.promaty.rrhh.dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vista mínima {id, name} de una FK de Project (type, specialty, client) para el DTO de detalle:
 * lo justo para que el frontend pre-cargue el selector correspondiente sin exponer la entidad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelationSummaryDto {

	private Long id;
	private String name;
}
