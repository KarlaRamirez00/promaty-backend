package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.MealType;

public interface MealTypeRepository extends JpaRepository<MealType, Long> {

	List<MealType> findByActiveTrueOrderByName();
}
