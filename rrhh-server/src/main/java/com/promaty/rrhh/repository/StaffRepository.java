package com.promaty.rrhh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.promaty.rrhh.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Long>, JpaSpecificationExecutor<Staff> {

	Optional<Staff> findByIdentificationNumber(String identificationNumber);

	@Query("""
		SELECT s FROM Staff s
		WHERE s.id NOT IN (SELECT c.staff.id FROM Contract c WHERE c.status.code = :statusCodeActivo)
		ORDER BY s.firstName, s.paternalLastName
		""")
	List<Staff> findAvailableForContractSelector(@Param("statusCodeActivo") String statusCodeActivo);
}
