package com.promaty.rrhh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.rrhh.entity.NonRehireReason;

public interface NonRehireReasonRepository extends JpaRepository<NonRehireReason, Long>, JpaSpecificationExecutor<NonRehireReason> {

	Optional<NonRehireReason> findByName(String name);

	Optional<NonRehireReason> findByCode(String code);
}
