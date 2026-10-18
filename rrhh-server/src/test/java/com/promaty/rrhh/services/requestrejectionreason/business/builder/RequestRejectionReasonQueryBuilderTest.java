package com.promaty.rrhh.services.requestrejectionreason.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonFilterParams;
import com.promaty.rrhh.entity.RequestRejectionReason;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings("unchecked")
class RequestRejectionReasonQueryBuilderTest {

	private final Root<RequestRejectionReason> root = mock(Root.class);
	private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
	private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

	@Test
	void fromFilters_conSubModule_agregaPredicadoDeIgualdad() {
		RequestRejectionReasonFilterParams filtros = new RequestRejectionReasonFilterParams();
		filtros.setSubModule("contracts");

		Path<String> subModulePath = mock(Path.class);
		Predicate subModulePredicate = mock(Predicate.class);
		Predicate combinado = mock(Predicate.class);
		when(root.<String>get("subModule")).thenReturn(subModulePath);
		when(cb.equal(subModulePath, "contracts")).thenReturn(subModulePredicate);
		ArgumentCaptor<Predicate[]> captor = ArgumentCaptor.forClass(Predicate[].class);
		when(cb.and(captor.capture())).thenReturn(combinado);

		Predicate resultado = RequestRejectionReasonQueryBuilder.fromFilters(filtros).toPredicate(root, query, cb);

		assertThat(resultado).isEqualTo(combinado);
		assertThat(captor.getValue()).containsExactly(subModulePredicate);
	}
}
