package com.promaty.rrhh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.rrhh.entity.ProjectSpecialty;

public interface ProjectSpecialtyRepository extends JpaRepository<ProjectSpecialty, Long>, JpaSpecificationExecutor<ProjectSpecialty> {

	Optional<ProjectSpecialty> findByName(String name);
}
