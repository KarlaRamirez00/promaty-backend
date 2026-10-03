package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.JobTitle;

public interface JobTitleRepository extends JpaRepository<JobTitle, Long> {

	List<JobTitle> findByActiveTrueOrderByName();
}
