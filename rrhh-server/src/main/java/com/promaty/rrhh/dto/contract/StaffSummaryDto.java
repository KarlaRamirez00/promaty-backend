package com.promaty.rrhh.dto.contract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vista mínima del colaborador para listado/detalle de Contract: nombre completo + RUT, que es lo
 * que el buscador de contratos filtra ("buscar por nombre o rut").
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffSummaryDto {

	private Long id;
	private String fullName;
	private String identificationNumber;
}
