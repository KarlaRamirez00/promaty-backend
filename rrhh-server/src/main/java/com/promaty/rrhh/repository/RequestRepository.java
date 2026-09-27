package com.promaty.rrhh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.rrhh.entity.Request;

public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {
}
