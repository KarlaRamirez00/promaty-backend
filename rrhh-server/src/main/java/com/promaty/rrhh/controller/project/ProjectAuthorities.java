package com.promaty.rrhh.controller.project;

public final class ProjectAuthorities {

	public static final String CAN_FILTER_BY_COST_CENTER = "hasAnyAuthority("
		+ "'project.read', 'staff.read', 'user.read', 'user.create', 'user.update', "
		+ "'contract.read', 'termination.read', 'annex.read', "
		+ "'contract.approve', 'contract.validate', "
		+ "'termination.approve', 'termination.validate', "
		+ "'annex.approve', 'annex.validate')";

	private ProjectAuthorities() {
	}
}
