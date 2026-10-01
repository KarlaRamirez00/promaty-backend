package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Provincia;

public interface ProvinciaRepository extends JpaRepository<Provincia, Long> {

	List<Provincia> findByRegionIdAndActiveTrueOrderByName(Long regionId);
}
