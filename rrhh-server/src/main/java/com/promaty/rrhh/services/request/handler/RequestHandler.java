package com.promaty.rrhh.services.request.handler;

import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;

public interface RequestHandler {

	RequestEntityType supports();

	/** Aplica el pendingData sobre la entidad real y retorna su id (creada o editada). */
	Long apply(Request request);
}
