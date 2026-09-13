package com.promaty.rrhh.services.project.business.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.dto.project.ProjectFilterParams;
import com.promaty.rrhh.entity.Project;

import jakarta.persistence.criteria.Predicate;

public final class ProjectQueryBuilder {

	private ProjectQueryBuilder() {
	}

	public static Specification<Project> fromFilters(ProjectFilterParams filters) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			if (filters.getSearch() != null && !filters.getSearch().isBlank()) {
				String patron = "%" + filters.getSearch().toLowerCase() + "%";
				Predicate porNombre = cb.like(cb.lower(root.get("name")), patron);
				Predicate porCentroCosto = cb.like(cb.lower(root.get("costCenterCode")), patron);
				predicates.add(cb.or(porNombre, porCentroCosto));
			}
			if (filters.getTypeId() != null) {
				predicates.add(cb.equal(root.get("type").get("id"), filters.getTypeId()));
			}
			if (filters.getSpecialtyId() != null) {
				predicates.add(cb.equal(root.get("specialty").get("id"), filters.getSpecialtyId()));
			}
			if (filters.getClientId() != null) {
				predicates.add(cb.equal(root.get("client").get("id"), filters.getClientId()));
			}
			if (filters.getStatusId() != null) {
				predicates.add(cb.equal(root.get("status").get("id"), filters.getStatusId()));
			}
			if (filters.getStartDateFrom() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filters.getStartDateFrom()));
			}
			if (filters.getStartDateTo() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), filters.getStartDateTo()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
