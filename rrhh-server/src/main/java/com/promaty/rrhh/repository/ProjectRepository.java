package com.promaty.rrhh.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
