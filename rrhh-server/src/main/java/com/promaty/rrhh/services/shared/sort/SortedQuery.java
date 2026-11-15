package com.promaty.rrhh.services.shared.sort;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public record SortedQuery<T>(Specification<T> specification, Pageable pageable) {
}
