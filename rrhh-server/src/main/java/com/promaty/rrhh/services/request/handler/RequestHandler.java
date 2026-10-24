package com.promaty.rrhh.services.request.handler;

import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;

public interface RequestHandler {

	RequestEntityType supports();

	/** Crea la entidad real desde el pendingData, en su estado inicial pendiente, y retorna su id. */
	Long create(Request request);

	/** Aplica el efecto de una decisión de aprobación/rechazo sobre la entidad ya creada por create(). */
	void applyDecision(Request request, ApprovalLevel level, ApprovalDecision decision);

	/** Permiso que habilita la decisión de nivel 1 (jefatura de centro de costo) para este tipo de solicitud. */
	String approvePermission();

	/** Permiso que habilita la decisión de nivel 2 (RRHH) para este tipo de solicitud. */
	String validatePermission();

	/** Permiso que habilita crear una solicitud de este tipo. */
	String createPermission();

	/** Permiso que habilita leer (listar/ver detalle) una solicitud de este tipo. */
	String readPermission();
}
