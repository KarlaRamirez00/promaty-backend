package com.promaty.rrhh.services.rexplus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Termination;

@Component
public class RexPlusStubClient implements RexPlusClient {

	private static final String PREFIJO_NUMERO_CONTRATO = "REX-";
	private static final String PREFIJO_NUMERO_FINIQUITO = "REX-TERM-";
	private static final String MENSAJE_ERROR_SIMULADO = "Rex+ no respondió (simulado por rexplus.stub.force-error).";

	private final boolean forzarError;

	public RexPlusStubClient(@Value("${rexplus.stub.force-error:false}") boolean forzarError) {
		this.forzarError = forzarError;
	}

	@Override
	public RexSyncResult syncContract(Contract contract) {
		if (forzarError) {
			return RexSyncResult.failure(MENSAJE_ERROR_SIMULADO);
		}
		return RexSyncResult.success(PREFIJO_NUMERO_CONTRATO + System.currentTimeMillis());
	}

	@Override
	public RexSyncResult syncTermination(Termination termination) {
		if (forzarError) {
			return RexSyncResult.failure(MENSAJE_ERROR_SIMULADO);
		}
		return RexSyncResult.success(PREFIJO_NUMERO_FINIQUITO + System.currentTimeMillis());
	}
}
