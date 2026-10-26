package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.repository.projection.RequestStaffDataProjection;

public interface TerminationRepository extends JpaRepository<Termination, Long>, JpaSpecificationExecutor<Termination> {

	List<Termination> findByContract_IdAndStatus_CodeIn(Long contractId, List<String> statusCodes);

	@Query("""
		select t.id as entityId,
			t.staff.identificationType as staffIdentificationType,
			t.staff.identificationNumber as staffIdentificationNumber,
			t.staff.firstName as staffFirstName,
			t.staff.paternalLastName as staffPaternalLastName,
			t.staff.maternalLastName as staffMaternalLastName,
			t.contract.jobTitle.name as jobTitleName
		from Termination t
		where t.id in :terminationIds
		""")
	List<RequestStaffDataProjection> findStaffDataByTerminationIds(@Param("terminationIds") List<Long> terminationIds);
}
