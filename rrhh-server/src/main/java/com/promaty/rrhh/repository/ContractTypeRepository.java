package com.promaty.rrhh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.ContractType;

public interface ContractTypeRepository extends JpaRepository<ContractType, Long> {

	List<ContractType> findByActiveTrueOrderByName();

	Optional<ContractType> findByCode(String code);
}
