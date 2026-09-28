package com.promaty.rrhh.dto.staff;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vista mínima {id, name} de una FK de Staff a un catálogo (registeredSex, maritalStatus,
 * nationality, educationLevel, afp, healthSystem, bank) para el DTO de detalle: lo justo para que
 * el frontend pre-cargue el selector correspondiente sin exponer la entidad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelationSummaryDto {

	private Long id;
	private String name;
	private String code;
}
