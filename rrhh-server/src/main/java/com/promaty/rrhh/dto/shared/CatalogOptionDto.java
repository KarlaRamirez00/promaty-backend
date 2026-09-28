package com.promaty.rrhh.dto.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Vista {id, name, code} para selectores de catálogos de solo lectura (sin CRUD) compartidos entre
 * varios dominios — ver convención en CLAUDE.md §4.
 */
@Getter
@AllArgsConstructor
public class CatalogOptionDto {

	private Long id;
	private String name;
	private String code;
}
