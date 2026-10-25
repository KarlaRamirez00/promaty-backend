package com.promaty.rrhh.services.termination.business;

import java.time.LocalDate;

import com.promaty.rrhh.dto.termination.TerminationDateRangeDto;

/**
 * Fuente unica de la regla de rango de terminationDate (hoy del cliente, 2026-10-25): minimo es el
 * mayor entre hoy-15 dias y el inicio del contrato; maximo es el fin del contrato, o sin tope si es
 * indefinido. Usado tanto para validar (RequestValidation) como para exponer el rango al selector de
 * Staff, sin duplicar la regla en dos lugares.
 */
public final class TerminationDateRangeCalculator {

	private static final int DIAS_MAXIMO_RETROACTIVO = 15;

	private TerminationDateRangeCalculator() {
	}

	public static TerminationDateRangeDto calculate(LocalDate contractStartDate, LocalDate contractEndDate) {
		LocalDate minimoRetroactivo = LocalDate.now().minusDays(DIAS_MAXIMO_RETROACTIVO);
		LocalDate minimo = contractStartDate.isAfter(minimoRetroactivo) ? contractStartDate : minimoRetroactivo;
		return new TerminationDateRangeDto(minimo, contractEndDate);
	}
}
