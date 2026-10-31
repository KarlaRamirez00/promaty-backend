package com.promaty.rrhh.services.contract.business.builder;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.entity.Contract;

public final class ContractExpirationSpecification {

	private static final String CODE_ACTIVE = "ACTIVE";

	private ContractExpirationSpecification() {
	}

	public static Specification<Contract> expiringSoon(LocalDate today, int days) {
		LocalDate limit = today.plusDays(days);
		return (root, query, cb) -> cb.and(
			cb.equal(root.get("status").get("code"), CODE_ACTIVE),
			cb.between(root.get("endDate"), today, limit)
		);
	}
}
