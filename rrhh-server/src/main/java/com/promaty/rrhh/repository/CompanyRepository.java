package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

	List<Company> findByActiveTrueOrderByName();
}
