package com.promaty.rrhh.services.shared;

import java.util.function.Function;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

public final class ProjectAccessSpecification {

	private ProjectAccessSpecification() {
	}

	public static <T> Specification<T> onProjectId() {
		return restrict(root -> root.get("id"));
	}

	public static <T> Specification<T> onProject() {
		return restrict(root -> root.get("project").get("id"));
	}

	private static <T> Specification<T> restrict(Function<Root<T>, Path<Long>> projectIdPath) {
		return (root, query, cb) -> CurrentUserProjectAccess.allowedAllProjects()
			? cb.conjunction()
			: projectIdPath.apply(root).in(CurrentUserProjectAccess.projectIds());
	}
}
