package com.promaty.rrhh.services.dashboard.business.builder;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class DashboardQueryBuilder {

	private static final String STATUS = "status";
	private static final String CODE = "code";
	private static final String STAFF = "staff";
	private static final String PROJECT = "project";
	private static final String START_DATE = "startDate";
	private static final String BIRTH_DATE = "birthDate";
	private static final String ENTITY_TYPE = "entityType";
	private static final String CODE_STAFF_BLOCKED = "BLOCKED";
	private static final List<String> CONTRACT_CODES_EVER_ACTIVE = List.of("ACTIVE", "EXPIRED", "ANNULLED", "TERMINATED");

	private DashboardQueryBuilder() {
	}

	public static <T> Specification<T> hasStatus(String code) {
		return (root, query, cb) -> cb.equal(root.get(STATUS).get(CODE), code);
	}

	public static <T> Specification<T> hasStatusIn(List<String> codes) {
		return (root, query, cb) -> root.get(STATUS).get(CODE).in(codes);
	}

	public static <T> Specification<T> dateBetween(String field, LocalDate from, LocalDate to) {
		return (root, query, cb) -> cb.between(root.get(field), from, to);
	}

	public static Specification<Request> ofEntityType(RequestEntityType entityType) {
		return (root, query, cb) -> cb.equal(root.get(ENTITY_TYPE), entityType);
	}

	public static Specification<Contract> staffBirthdayInMonth(int month) {
		return (root, query, cb) -> {
			Expression<Double> birthMonth = cb.function(
				"date_part", Double.class, cb.literal("month"), root.get(STAFF).get(BIRTH_DATE)
			);
			return cb.equal(birthMonth, (double) month);
		};
	}

	public static Specification<Contract> latestContractOfBlockedStaff() {
		return (root, query, cb) -> {
			Subquery<LocalDate> latestStartDate = query.subquery(LocalDate.class);
			Root<Contract> other = latestStartDate.from(Contract.class);
			latestStartDate.select(cb.greatest(other.<LocalDate>get(START_DATE)))
				.where(
					cb.equal(other.get(STAFF), root.get(STAFF)),
					other.get(STATUS).get(CODE).in(CONTRACT_CODES_EVER_ACTIVE)
				);
			return cb.and(
				cb.equal(root.get(STAFF).get(STATUS).get(CODE), CODE_STAFF_BLOCKED),
				root.get(STATUS).get(CODE).in(CONTRACT_CODES_EVER_ACTIVE),
				cb.equal(root.get(START_DATE), latestStartDate)
			);
		};
	}

	public static Specification<Contract> fetchingStaffAndProject() {
		return (root, query, cb) -> {
			root.fetch(STAFF);
			root.fetch(PROJECT);
			return cb.conjunction();
		};
	}
}
