package com.promaty.rrhh.services.contract.business.builder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.contract.UpdateContractDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.CompanyRepository;
import com.promaty.rrhh.repository.ContractTypeRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.MealTypeRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.TransportTypeRepository;

@Component
public class UpdateContractBuilder {

	private final ProjectRepository projectRepository;
	private final CompanyRepository companyRepository;
	private final ContractTypeRepository contractTypeRepository;
	private final JobTitleRepository jobTitleRepository;
	private final SiteRepository siteRepository;
	private final MealTypeRepository mealTypeRepository;
	private final TransportTypeRepository transportTypeRepository;

	public UpdateContractBuilder(
		ProjectRepository projectRepository,
		CompanyRepository companyRepository,
		ContractTypeRepository contractTypeRepository,
		JobTitleRepository jobTitleRepository,
		SiteRepository siteRepository,
		MealTypeRepository mealTypeRepository,
		TransportTypeRepository transportTypeRepository
	) {
		this.projectRepository = projectRepository;
		this.companyRepository = companyRepository;
		this.contractTypeRepository = contractTypeRepository;
		this.jobTitleRepository = jobTitleRepository;
		this.siteRepository = siteRepository;
		this.mealTypeRepository = mealTypeRepository;
		this.transportTypeRepository = transportTypeRepository;
	}

	public void apply(Contract contract, UpdateContractDto dto) {
		contract.setProject(find(projectRepository, dto.getProjectId(), "El centro de costo indicado no existe."));
		contract.setCompany(find(companyRepository, dto.getCompanyId(), "La empresa indicada no existe."));
		contract.setContractType(find(contractTypeRepository, dto.getContractTypeId(), "El tipo de contrato indicado no existe."));
		contract.setJobTitle(find(jobTitleRepository, dto.getJobTitleId(), "El cargo indicado no existe."));
		contract.setSite(find(siteRepository, dto.getSiteId(), "La sucursal indicada no existe."));
		contract.setStartDate(dto.getStartDate());
		contract.setEndDate(dto.getEndDate());
		contract.setBaseSalary(dto.getBaseSalary());
		contract.setAgreedSalary(dto.getAgreedSalary());
		contract.setWeeklyWorkHours(dto.getWeeklyWorkHours());
		contract.setWorkDays(dto.getWorkDays());
		contract.setContractDetail(dto.getContractDetail());
		contract.setMealType(findOptional(mealTypeRepository, dto.getMealTypeId(), "El tipo de colacion indicado no existe."));
		contract.setTransportType(findOptional(transportTypeRepository, dto.getTransportTypeId(), "El tipo de movilizacion indicado no existe."));
	}

	private static <T> T find(JpaRepository<T, Long> repository, Long id, String notFoundMessage) {
		return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(notFoundMessage));
	}

	private static <T> T findOptional(JpaRepository<T, Long> repository, Long id, String notFoundMessage) {
		return id == null ? null : find(repository, id, notFoundMessage);
	}
}
