package com.promaty.rrhh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Contract;

public interface ContractRepository extends JpaRepository<Contract, Long> {

	Optional<Contract> findByStaffIdAndStatus_Code(Long staffId, String statusCode);

	Optional<Contract> findByContractNumber(String contractNumber);
}
