package com.promaty.rrhh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Bank;

public interface BankRepository extends JpaRepository<Bank, Long> {

	Optional<Bank> findByCode(String code);

	List<Bank> findByActiveTrueOrderByName();
}
