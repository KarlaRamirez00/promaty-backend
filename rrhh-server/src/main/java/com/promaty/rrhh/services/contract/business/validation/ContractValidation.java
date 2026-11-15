package com.promaty.rrhh.services.contract.business.validation;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.contract.ContractFieldsDto;
import com.promaty.rrhh.dto.contract.UpdateContractDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.CompanyRepository;
import com.promaty.rrhh.repository.ContractTypeRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.MealTypeRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.TransportTypeRepository;
import com.promaty.rrhh.services.shared.CurrentUserProjectAccess;

@Component
public class ContractValidation {

	private static final String VALIDATION_MESSAGE = "La validacion fallo para uno o mas campos.";
	private static final String CODE_FIXED_TERM = "F";
	private static final String FIELD_PROJECT = "projectId";

	private final ProjectRepository projectRepository;
	private final CompanyRepository companyRepository;
	private final ContractTypeRepository contractTypeRepository;
	private final JobTitleRepository jobTitleRepository;
	private final SiteRepository siteRepository;
	private final MealTypeRepository mealTypeRepository;
	private final TransportTypeRepository transportTypeRepository;

	public ContractValidation(
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

	public void validateUpdate(Contract contract, UpdateContractDto dto) {
		Map<String, String> errors = new LinkedHashMap<>();
		collectProjectErrors(dto.getProjectId(), errors);
		collectFieldErrors(dto, contract.getStartDate(), errors);
		if (!errors.isEmpty()) {
			throw new BusinessValidationException(VALIDATION_MESSAGE, errors);
		}
	}

	public void collectFieldErrors(ContractFieldsDto fields, LocalDate currentStartDate, Map<String, String> errors) {
		collectRelationErrors(fields, errors);
		collectDateErrors(fields, currentStartDate, errors);
	}

	public void collectProjectErrors(Long projectId, Map<String, String> errors) {
		if (projectId == null) {
			return;
		}
		if (!projectRepository.existsById(projectId)) {
			errors.put(FIELD_PROJECT, "El centro de costo indicado no existe.");
			return;
		}
		if (!CurrentUserProjectAccess.allowedAllProjects() && !CurrentUserProjectAccess.projectIds().contains(projectId)) {
			errors.put(FIELD_PROJECT, "No tienes acceso al centro de costo indicado.");
		}
	}

	private void collectRelationErrors(ContractFieldsDto fields, Map<String, String> errors) {
		if (fields.getCompanyId() != null && !companyRepository.existsById(fields.getCompanyId())) {
			errors.put("companyId", "La empresa indicada no existe.");
		}
		if (fields.getContractTypeId() != null && !contractTypeRepository.existsById(fields.getContractTypeId())) {
			errors.put("contractTypeId", "El tipo de contrato indicado no existe.");
		}
		if (fields.getJobTitleId() != null && !jobTitleRepository.existsById(fields.getJobTitleId())) {
			errors.put("jobTitleId", "El cargo indicado no existe.");
		}
		if (fields.getSiteId() != null && !siteRepository.existsById(fields.getSiteId())) {
			errors.put("siteId", "La sucursal indicada no existe.");
		}
		if (fields.getMealTypeId() != null && !mealTypeRepository.existsById(fields.getMealTypeId())) {
			errors.put("mealTypeId", "El tipo de colacion indicado no existe.");
		}
		if (fields.getTransportTypeId() != null && !transportTypeRepository.existsById(fields.getTransportTypeId())) {
			errors.put("transportTypeId", "El tipo de movilizacion indicado no existe.");
		}
	}

	private void collectDateErrors(ContractFieldsDto fields, LocalDate currentStartDate, Map<String, String> errors) {
		LocalDate startDate = fields.getStartDate();
		boolean startDateChanged = startDate != null && !startDate.equals(currentStartDate);
		if (startDateChanged && startDate.isBefore(LocalDate.now())) {
			errors.put("startDate", "La fecha de inicio no puede ser anterior a hoy.");
		}
		if (startDate != null && fields.getEndDate() != null && fields.getEndDate().isBefore(startDate)) {
			errors.put("endDate", "La fecha de termino no puede ser anterior a la fecha de inicio.");
		}
		if (fields.getEndDate() == null && requiresEndDate(fields.getContractTypeId())) {
			errors.put("endDate", "La fecha de termino es obligatoria para un contrato a plazo fijo.");
		}
	}

	private boolean requiresEndDate(Long contractTypeId) {
		if (contractTypeId == null) {
			return false;
		}
		return contractTypeRepository.findById(contractTypeId)
			.map(contractType -> CODE_FIXED_TERM.equals(contractType.getCode()))
			.orElse(false);
	}
}
