package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.HealthSystem;

public interface HealthSystemRepository extends JpaRepository<HealthSystem, Long> {

	List<HealthSystem> findByActiveTrueOrderByName();
}
