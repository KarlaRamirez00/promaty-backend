package com.promaty.rrhh.services.contract.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.services.shared.StaffSearchPredicate;

import jakarta.persistence.criteria.Predicate;

public final class ContractQueryBuilder {

	private ContractQueryBuilder() {
	}

	public static Specification<Contract> fromFilters(ContractFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				predicates.add(StaffSearchPredicate.matchesNameOrRut(cb, root.get("staff"), filters.getSearch()));
			}
			if (filters.getProjectId() != null) {
				predicates.add(cb.equal(root.get("project").get("id"), filters.getProjectId()));
			}
			if (filters.getStatusId() != null) {
				predicates.add(cb.equal(root.get("status").get("id"), filters.getStatusId()));
			}
			if (filters.getContractTypeId() != null) {
				predicates.add(cb.equal(root.get("contractType").get("id"), filters.getContractTypeId()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
