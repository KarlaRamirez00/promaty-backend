package com.promaty.rrhh.services.rexplus;

import com.promaty.rrhh.entity.Contract;

/**
 * Puerto hacia el ERP de RRHH (Rex+). Fuera de alcance v1 el cliente real con llamada de red — ver
 * RexPlusStubClient (adaptador sin red, ver rrhh-domain.md seccion 1).
 */
public interface RexPlusClient {

	RexSyncResult syncContract(Contract contract);
}
