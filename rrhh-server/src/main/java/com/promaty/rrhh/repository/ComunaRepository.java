package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Comuna;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {

	List<Comuna> findByProvinciaIdAndActiveTrueOrderByName(Long provinciaId);
}
