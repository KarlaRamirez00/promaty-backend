package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.rrhh.entity.Termination;

public interface TerminationRepository extends JpaRepository<Termination, Long>, JpaSpecificationExecutor<Termination> {

	List<Termination> findByContract_IdAndStatus_CodeIn(Long contractId, List<String> statusCodes);
}
