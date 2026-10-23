package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.LegalTerminationCause;

public interface LegalTerminationCauseRepository extends JpaRepository<LegalTerminationCause, Long> {

	List<LegalTerminationCause> findByActiveTrueOrderByName();
}
