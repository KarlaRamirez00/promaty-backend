package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.PlatformStatus;

public interface PlatformStatusRepository extends JpaRepository<PlatformStatus, Long> {

	List<PlatformStatus> findBySubModuleAndActiveTrueOrderBySortOrder(String subModule);
}
