package com.promaty.rrhh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;

public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {

	Optional<Request> findByEntityTypeAndEntityId(RequestEntityType entityType, Long entityId);

	List<Request> findByEntityTypeAndEntityIdIn(RequestEntityType entityType, List<Long> entityIds);
}
