package com.promaty.rrhh.services.dashboard.business;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import com.promaty.rrhh.services.shared.CurrentUserProjectAccess;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;

public final class DashboardScope {

	private static final String PROJECT = "project";
	private static final String ID = "id";

	private final Long projectId;

	private DashboardScope(Long projectId) {
		this.projectId = projectId;
	}

	public static DashboardScope of(Long projectId) {
		boolean outOfReach = projectId != null
			&& !CurrentUserProjectAccess.allowedAllProjects()
			&& !CurrentUserProjectAccess.projectIds().contains(projectId);
		if (outOfReach) {
			throw new AccessDeniedException("El proyecto indicado está fuera del acceso del usuario.");
		}
		return new DashboardScope(projectId);
	}

	public boolean coversEveryProject() {
		return projectId == null && CurrentUserProjectAccess.allowedAllProjects();
	}

	public <T> Specification<T> onProject() {
		Specification<T> access = ProjectAccessSpecification.onProject();
		return projectId == null
			? access
			: access.and((root, query, cb) -> cb.equal(root.get(PROJECT).get(ID), projectId));
	}

	public <T> Specification<T> onProjectId() {
		Specification<T> access = ProjectAccessSpecification.onProjectId();
		return projectId == null
			? access
			: access.and((root, query, cb) -> cb.equal(root.get(ID), projectId));
	}
}
