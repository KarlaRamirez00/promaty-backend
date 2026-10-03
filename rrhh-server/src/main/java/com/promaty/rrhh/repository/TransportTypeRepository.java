package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.TransportType;

public interface TransportTypeRepository extends JpaRepository<TransportType, Long> {

	List<TransportType> findByActiveTrueOrderByName();
}
