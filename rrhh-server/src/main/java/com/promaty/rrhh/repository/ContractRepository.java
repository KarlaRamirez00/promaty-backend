package com.promaty.rrhh.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.rrhh.entity.Contract;

public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

	Optional<Contract> findByStaffIdAndStatus_Code(Long staffId, String statusCode);

	Optional<Contract> findByContractNumber(String contractNumber);

	List<Contract> findByStatus_CodeAndEndDateBefore(String statusCode, LocalDate date);
}
