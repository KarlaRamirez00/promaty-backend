package com.promaty.rrhh.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.repository.projection.RequestStaffDataProjection;
import com.promaty.rrhh.repository.projection.StaffActiveContractDateRangeProjection;
import com.promaty.rrhh.repository.projection.StaffContractStartDateProjection;
import com.promaty.rrhh.repository.projection.StaffCostCenterProjection;

public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

	Optional<Contract> findByStaffIdAndStatus_Code(Long staffId, String statusCode);

	Optional<Contract> findByRexContractNumber(String rexContractNumber);

	List<Contract> findByStatus_CodeAndEndDateBefore(String statusCode, LocalDate date);

	Optional<Contract> findFirstByStaffIdAndStatus_CodeInOrderByStartDateDesc(
		Long staffId,
		List<String> statusCodes
	);

	@Query("""
		select c.staff.id as staffId, c.project.costCenterCode as costCenterCode
		from Contract c
		where c.staff.id in :staffIds and c.status.code = :statusCode
		""")
	List<StaffCostCenterProjection> findActiveCostCentersByStaffIds(
		@Param("staffIds") List<Long> staffIds,
		@Param("statusCode") String statusCode
	);

	@Query("""
		select c.staff.id as staffId, c.startDate as startDate
		from Contract c
		where c.staff.id in :staffIds and c.status.code in :statusCodes
		""")
	List<StaffContractStartDateProjection> findStartDatesByStaffIds(
		@Param("staffIds") List<Long> staffIds,
		@Param("statusCodes") List<String> statusCodes
	);

	@Query("""
		select c.id as entityId,
			c.staff.identificationType as staffIdentificationType,
			c.staff.identificationNumber as staffIdentificationNumber,
			c.staff.firstName as staffFirstName,
			c.staff.paternalLastName as staffPaternalLastName,
			c.staff.maternalLastName as staffMaternalLastName,
			c.jobTitle.name as jobTitleName
		from Contract c
		where c.id in :contractIds
		""")
	List<RequestStaffDataProjection> findStaffDataByContractIds(@Param("contractIds") List<Long> contractIds);

	@Query("""
		select c.staff.id as staffId, c.startDate as startDate, c.endDate as endDate
		from Contract c
		where c.staff.id in :staffIds and c.status.code = :statusCode
		""")
	List<StaffActiveContractDateRangeProjection> findActiveContractDateRangesByStaffIds(
		@Param("staffIds") List<Long> staffIds,
		@Param("statusCode") String statusCode
	);
}
