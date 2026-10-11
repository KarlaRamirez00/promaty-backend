package com.promaty.rrhh.services.request.business.mapper;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.rrhh.dto.contract.RelationSummaryDto;
import com.promaty.rrhh.dto.contract.StaffSummaryDto;
import com.promaty.rrhh.dto.request.ContractPendingDataDto;
import com.promaty.rrhh.dto.request.ContractPendingDataResolvedDto;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.repository.CompanyRepository;
import com.promaty.rrhh.repository.ContractTypeRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.MealTypeRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TransportTypeRepository;

@Component
public class ContractPendingDataResolver {

	private final ObjectMapper objectMapper;
	private final StaffRepository staffRepository;
	private final CompanyRepository companyRepository;
	private final ContractTypeRepository contractTypeRepository;
	private final JobTitleRepository jobTitleRepository;
	private final SiteRepository siteRepository;
	private final MealTypeRepository mealTypeRepository;
	private final TransportTypeRepository transportTypeRepository;

	public ContractPendingDataResolver(
		ObjectMapper objectMapper,
		StaffRepository staffRepository,
		CompanyRepository companyRepository,
		ContractTypeRepository contractTypeRepository,
		JobTitleRepository jobTitleRepository,
		SiteRepository siteRepository,
		MealTypeRepository mealTypeRepository,
		TransportTypeRepository transportTypeRepository
	) {
		this.objectMapper = objectMapper;
		this.staffRepository = staffRepository;
		this.companyRepository = companyRepository;
		this.contractTypeRepository = contractTypeRepository;
		this.jobTitleRepository = jobTitleRepository;
		this.siteRepository = siteRepository;
		this.mealTypeRepository = mealTypeRepository;
		this.transportTypeRepository = transportTypeRepository;
	}

	public ContractPendingDataResolvedDto resolve(String pendingDataJson) {
		ContractPendingDataDto datos = deserializar(pendingDataJson);
		return new ContractPendingDataResolvedDto(
			staffRepository.findById(datos.getStaffId()).map(this::toStaffSummary).orElse(null),
			companyRepository.findById(datos.getCompanyId()).map(c -> toRelationSummary(c.getId(), c.getName())).orElse(null),
			contractTypeRepository.findById(datos.getContractTypeId()).map(ct -> toRelationSummary(ct.getId(), ct.getName())).orElse(null),
			jobTitleRepository.findById(datos.getJobTitleId()).map(jt -> toRelationSummary(jt.getId(), jt.getName())).orElse(null),
			siteRepository.findById(datos.getSiteId()).map(s -> toRelationSummary(s.getId(), s.getName())).orElse(null),
			datos.getStartDate(),
			datos.getEndDate(),
			datos.getBaseSalary(),
			datos.getAgreedSalary(),
			datos.getWeeklyWorkHours(),
			datos.getWorkDays(),
			datos.getContractDetail(),
			resolverMealType(datos.getMealTypeId()),
			resolverTransportType(datos.getTransportTypeId())
		);
	}

	private RelationSummaryDto resolverMealType(Long id) {
		if (id == null) {
			return null;
		}
		return mealTypeRepository.findById(id).map(mt -> toRelationSummary(mt.getId(), mt.getName())).orElse(null);
	}

	private RelationSummaryDto resolverTransportType(Long id) {
		if (id == null) {
			return null;
		}
		return transportTypeRepository.findById(id).map(tt -> toRelationSummary(tt.getId(), tt.getName())).orElse(null);
	}

	private StaffSummaryDto toStaffSummary(Staff staff) {
		return new StaffSummaryDto(staff.getId(), staff.getFullName(), staff.getIdentificationNumber());
	}

	private RelationSummaryDto toRelationSummary(Long id, String name) {
		return new RelationSummaryDto(id, name);
	}

	private ContractPendingDataDto deserializar(String pendingData) {
		try {
			return objectMapper.readValue(pendingData, ContractPendingDataDto.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("El pendingData de la solicitud es invalido.", e);
		}
	}
}
