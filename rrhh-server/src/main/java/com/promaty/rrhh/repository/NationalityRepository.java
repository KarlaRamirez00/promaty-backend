package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Nationality;

public interface NationalityRepository extends JpaRepository<Nationality, Long> {

	List<Nationality> findByActiveTrueOrderByName();
}
