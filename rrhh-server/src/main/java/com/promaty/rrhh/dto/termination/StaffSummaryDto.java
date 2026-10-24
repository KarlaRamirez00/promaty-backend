package com.promaty.rrhh.dto.termination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vista mínima del colaborador para listado/detalle de Termination: nombre completo + RUT, que es lo
 * que el buscador filtra ("buscar por nombre o rut") — mismo patrón que dto/contract.
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
