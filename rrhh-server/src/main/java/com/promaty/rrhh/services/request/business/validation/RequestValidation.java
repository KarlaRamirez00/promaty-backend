package com.promaty.rrhh.services.request.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.request.ContractPendingDataDto;
import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.CompanyRepository;
import com.promaty.rrhh.repository.ContractTypeRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.MealTypeRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TransportTypeRepository;

@Component
public class RequestValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";

	private final ProjectRepository projectRepository;
	private final StaffRepository staffRepository;
	private final CompanyRepository companyRepository;
	private final ContractTypeRepository contractTypeRepository;
	private final JobTitleRepository jobTitleRepository;
	private final SiteRepository siteRepository;
	private final MealTypeRepository mealTypeRepository;
	private final TransportTypeRepository transportTypeRepository;

	public RequestValidation(
		ProjectRepository projectRepository,
		StaffRepository staffRepository,
		CompanyRepository companyRepository,
		ContractTypeRepository contractTypeRepository,
		JobTitleRepository jobTitleRepository,
		SiteRepository siteRepository,
		MealTypeRepository mealTypeRepository,
		TransportTypeRepository transportTypeRepository
	) {
		this.projectRepository = projectRepository;
		this.staffRepository = staffRepository;
		this.companyRepository = companyRepository;
		this.contractTypeRepository = contractTypeRepository;
		this.jobTitleRepository = jobTitleRepository;
		this.siteRepository = siteRepository;
		this.mealTypeRepository = mealTypeRepository;
		this.transportTypeRepository = transportTypeRepository;
	}

	public void validateCreate(CreateRequestDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (dto.getProjectId() != null && !projectRepository.existsById(dto.getProjectId())) {
			errores.put("projectId", "El centro de costo indicado no existe.");
		}

		if (dto.getEntityType() == RequestEntityType.CONTRACT) {
			validarSolicitudDeContrato(dto, errores);
		}

		lanzarSiHayErrores(errores);
	}

	private void validarSolicitudDeContrato(CreateRequestDto dto, Map<String, String> errores) {
		if (dto.getAction() != RequestAction.CREATE) {
			errores.put("action", "Por ahora solo se soporta crear un contrato nuevo (CREATE).");
			return;
		}
		ContractPendingDataDto datos = dto.getContractData();
		if (datos == null) {
			errores.put("contractData", "Los datos del contrato son obligatorios para este tipo de solicitud.");
			return;
		}

		validarRelaciones(errores, datos);
		validarFechas(errores, datos);
	}

	private void validarRelaciones(Map<String, String> errores, ContractPendingDataDto datos) {
		if (datos.getStaffId() != null && !staffRepository.existsById(datos.getStaffId())) {
			errores.put("staffId", "El colaborador indicado no existe.");
		}
		if (datos.getCompanyId() != null && !companyRepository.existsById(datos.getCompanyId())) {
			errores.put("companyId", "La empresa indicada no existe.");
		}
		if (datos.getContractTypeId() != null && !contractTypeRepository.existsById(datos.getContractTypeId())) {
			errores.put("contractTypeId", "El tipo de contrato indicado no existe.");
		}
		if (datos.getJobTitleId() != null && !jobTitleRepository.existsById(datos.getJobTitleId())) {
			errores.put("jobTitleId", "El cargo indicado no existe.");
		}
		if (datos.getSiteId() != null && !siteRepository.existsById(datos.getSiteId())) {
			errores.put("siteId", "La sucursal indicada no existe.");
		}
		if (datos.getMealTypeId() != null && !mealTypeRepository.existsById(datos.getMealTypeId())) {
			errores.put("mealTypeId", "El tipo de colacion indicado no existe.");
		}
		if (datos.getTransportTypeId() != null && !transportTypeRepository.existsById(datos.getTransportTypeId())) {
			errores.put("transportTypeId", "El tipo de movilizacion indicado no existe.");
		}
	}

	private void validarFechas(Map<String, String> errores, ContractPendingDataDto datos) {
		if (datos.getStartDate() != null && datos.getEndDate() != null
			&& datos.getEndDate().isBefore(datos.getStartDate())) {
			errores.put("endDate", "La fecha de termino no puede ser anterior a la fecha de inicio.");
		}
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
