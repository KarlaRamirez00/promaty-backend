package com.promaty.rrhh.services.request.business.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;

class RequestTypeLabelResolverTest {

	@Test
	void resolve_contractCreate_retornaEtiquetaEspanola() {
		String etiqueta = RequestTypeLabelResolver.resolve(RequestEntityType.CONTRACT, RequestAction.CREATE);

		assertThat(etiqueta).isEqualTo("Nuevo contrato");
	}

	@Test
	void resolve_conEntityTypeNulo_retornaNullSinLanzarExcepcion() {
		String etiqueta = RequestTypeLabelResolver.resolve(null, RequestAction.CREATE);

		assertThat(etiqueta).isNull();
	}

	@Test
	void resolve_conActionNula_retornaNullSinLanzarExcepcion() {
		String etiqueta = RequestTypeLabelResolver.resolve(RequestEntityType.CONTRACT, null);

		assertThat(etiqueta).isNull();
	}
}
