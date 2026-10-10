package com.promaty.rrhh.services.rexplus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Contract;

@Component
public class RexPlusStubClient implements RexPlusClient {

	private static final String PREFIJO_NUMERO_CONTRATO = "REX-";

	private final boolean forzarError;

	public RexPlusStubClient(@Value("${rexplus.stub.force-error:false}") boolean forzarError) {
		this.forzarError = forzarError;
	}

	@Override
	public RexSyncResult syncContract(Contract contract) {
		if (forzarError) {
			return RexSyncResult.failure("Rex+ no respondió (simulado por rexplus.stub.force-error).");
		}
		return RexSyncResult.success(PREFIJO_NUMERO_CONTRATO + System.currentTimeMillis());
	}
}
