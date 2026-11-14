package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.AnnexType;

public interface AnnexTypeRepository extends JpaRepository<AnnexType, Long> {

	List<AnnexType> findByActiveTrueOrderByName();
}
