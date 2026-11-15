package com.promaty.rrhh.services.shared.sort;

import java.util.List;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;

public final class StaffSortPaths {

	private StaffSortPaths() {
	}

	public static List<Expression<?>> fullName(Path<?> staff) {
		return List.of(staff.get("firstName"), staff.get("paternalLastName"), staff.get("maternalLastName"));
	}

	public static List<Expression<?>> identificationNumber(Path<?> staff) {
		return List.of(staff.get("identificationNumber"));
	}
}
