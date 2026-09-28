package com.promaty.rrhh.dto.bank;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Selector de Bank: además de {id, name, code}, expone supportsRutAccount para que frontend sepa si
 * "Cuenta Rut" es una opción válida sin tener que conocer el code exacto de BancoEstado.
 */
@Getter
@AllArgsConstructor
public class BankOptionDto {

	private Long id;
	private String name;
	private String code;
	private boolean supportsRutAccount;
}
