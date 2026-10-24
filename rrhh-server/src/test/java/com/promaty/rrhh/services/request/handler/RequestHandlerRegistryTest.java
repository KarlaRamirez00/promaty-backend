package com.promaty.rrhh.services.request.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class RequestHandlerRegistryTest {

	@Mock
	private RequestHandler contractHandler;

	@Test
	void get_conEntityTypeRegistrado_retornaElHandlerCorrespondiente() {
		when(contractHandler.supports()).thenReturn(RequestEntityType.CONTRACT);
		RequestHandlerRegistry registry = new RequestHandlerRegistry(List.of(contractHandler));

		assertThat(registry.get(RequestEntityType.CONTRACT)).isEqualTo(contractHandler);
	}

	@Test
	void get_conEntityTypeSinHandlerRegistrado_lanzaResourceNotFound() {
		RequestHandlerRegistry registry = new RequestHandlerRegistry(List.of());

		assertThatThrownBy(() -> registry.get(RequestEntityType.CONTRACT))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void entityTypesReadableBy_retornaSoloLosTiposConPermisoDeLectura() {
		when(contractHandler.supports()).thenReturn(RequestEntityType.CONTRACT);
		when(contractHandler.readPermission()).thenReturn("contract.read");
		RequestHandler terminationHandler = mock(RequestHandler.class);
		when(terminationHandler.supports()).thenReturn(RequestEntityType.TERMINATION);
		when(terminationHandler.readPermission()).thenReturn("termination.read");
		RequestHandlerRegistry registry = new RequestHandlerRegistry(List.of(contractHandler, terminationHandler));

		List<RequestEntityType> legibles = registry.entityTypesReadableBy(Set.of("contract.read"));

		assertThat(legibles).containsExactly(RequestEntityType.CONTRACT);
	}
}
