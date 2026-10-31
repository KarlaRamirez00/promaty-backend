package com.promaty.rrhh.services.contract.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.promaty.rrhh.entity.Contract;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings("unchecked")
class ContractExpirationSpecificationTest {

	private final Root<Contract> root = mock(Root.class);
	private final CriteriaQuery<?> query = mock(CriteriaQuery.class);
	private final CriteriaBuilder cb = mock(CriteriaBuilder.class);

	@Test
	void expiringSoon_exigeEstadoActivoYEndDateEntreHoyYHoyMasDias() {
		LocalDate today = LocalDate.of(2026, 10, 31);
		Path<Object> statusPath = mock(Path.class);
		Path<Object> codePath = mock(Path.class);
		Path<LocalDate> endDatePath = mock(Path.class);
		Predicate activePredicate = mock(Predicate.class);
		Predicate rangePredicate = mock(Predicate.class);
		Predicate combined = mock(Predicate.class);
		when(root.get("status")).thenReturn(statusPath);
		when(statusPath.get("code")).thenReturn(codePath);
		when(root.<LocalDate>get("endDate")).thenReturn(endDatePath);
		when(cb.equal(codePath, "ACTIVE")).thenReturn(activePredicate);
		when(cb.between(endDatePath, today, LocalDate.of(2026, 11, 15))).thenReturn(rangePredicate);
		ArgumentCaptor<Predicate> captor = ArgumentCaptor.forClass(Predicate.class);
		when(cb.and(captor.capture(), captor.capture())).thenReturn(combined);

		Predicate result = ContractExpirationSpecification.expiringSoon(today, 15).toPredicate(root, query, cb);

		assertThat(result).isEqualTo(combined);
		assertThat(captor.getAllValues()).containsExactly(activePredicate, rangePredicate);
	}
}
