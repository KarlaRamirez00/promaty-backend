package com.promaty.rrhh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.rrhh.entity.RequestRejectionReason;

public interface RequestRejectionReasonRepository
		extends JpaRepository<RequestRejectionReason, Long>, JpaSpecificationExecutor<RequestRejectionReason> {

	Optional<RequestRejectionReason> findByName(String name);

	List<RequestRejectionReason> findBySubModuleAndActiveTrueOrderByName(String subModule);
}
