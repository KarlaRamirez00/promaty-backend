package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.EducationLevel;

public interface EducationLevelRepository extends JpaRepository<EducationLevel, Long> {

	List<EducationLevel> findByActiveTrueOrderByName();
}
