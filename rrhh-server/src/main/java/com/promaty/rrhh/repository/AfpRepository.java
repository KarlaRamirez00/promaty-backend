package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Afp;

public interface AfpRepository extends JpaRepository<Afp, Long> {

	List<Afp> findByActiveTrueOrderByName();
}
