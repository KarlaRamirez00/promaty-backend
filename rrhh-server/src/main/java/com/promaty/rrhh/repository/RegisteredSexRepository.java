package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.RegisteredSex;

public interface RegisteredSexRepository extends JpaRepository<RegisteredSex, Long> {

	List<RegisteredSex> findByActiveTrueOrderByName();
}
