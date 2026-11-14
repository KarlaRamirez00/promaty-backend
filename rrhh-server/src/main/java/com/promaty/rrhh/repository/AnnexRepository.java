package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.repository.projection.RequestStaffDataProjection;

public interface AnnexRepository extends JpaRepository<Annex, Long>, JpaSpecificationExecutor<Annex> {

	boolean existsByContract_IdAndStatus_CodeIn(Long contractId, List<String> statusCodes);

	boolean existsByContract_IdAndAnnexType_CodeAndStatus_Code(Long contractId, AnnexTypeCode typeCode, String statusCode);

	@Query("""
		select a.id as entityId,
			a.staff.identificationType as staffIdentificationType,
			a.staff.identificationNumber as staffIdentificationNumber,
			a.staff.firstName as staffFirstName,
			a.staff.paternalLastName as staffPaternalLastName,
			a.staff.maternalLastName as staffMaternalLastName,
			a.contract.jobTitle.name as jobTitleName
		from Annex a
		where a.id in :annexIds
		""")
	List<RequestStaffDataProjection> findStaffDataByAnnexIds(@Param("annexIds") List<Long> annexIds);
}
