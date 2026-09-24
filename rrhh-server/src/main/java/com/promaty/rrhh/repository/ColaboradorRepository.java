package com.promaty.rrhh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.promaty.rrhh.entity.Colaborador;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long>, JpaSpecificationExecutor<Colaborador> {

	Optional<Colaborador> findByIdentificationNumber(String identificationNumber);
}
