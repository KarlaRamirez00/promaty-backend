package com.promaty.rrhh.services.request.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.rrhh.dto.request.ContractPendingDataDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.CompanyRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.ContractTypeRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.MealTypeRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TransportTypeRepository;

/**
 * RequestValidation ya confirmó que las FK de contractData existen al crear la Request; para cuando
 * este Handler corre (después de 2 niveles de aprobación) pueden haber pasado días, de ahí el
 * orElseThrow defensivo en cada resolución.
 */
@Component
public class ContractRequestHandler implements RequestHandler {

	private static final String SUBMODULE_CONTRACT = "contract";
	private static final String CODE_ACTIVE = "ACTIVE";

	private final ObjectMapper objectMapper;
	private final ContractRepository contractRepository;
	private final StaffRepository staffRepository;
	private final CompanyRepository companyRepository;
	private final ContractTypeRepository contractTypeRepository;
	private final JobTitleRepository jobTitleRepository;
	private final SiteRepository siteRepository;
	private final MealTypeRepository mealTypeRepository;
	private final TransportTypeRepository transportTypeRepository;
	private final PlatformStatusRepository platformStatusRepository;

	public ContractRequestHandler(
		ObjectMapper objectMapper,
		ContractRepository contractRepository,
		StaffRepository staffRepository,
		CompanyRepository companyRepository,
		ContractTypeRepository contractTypeRepository,
		JobTitleRepository jobTitleRepository,
		SiteRepository siteRepository,
		MealTypeRepository mealTypeRepository,
		TransportTypeRepository transportTypeRepository,
		PlatformStatusRepository platformStatusRepository
	) {
		this.objectMapper = objectMapper;
		this.contractRepository = contractRepository;
		this.staffRepository = staffRepository;
		this.companyRepository = companyRepository;
		this.contractTypeRepository = contractTypeRepository;
		this.jobTitleRepository = jobTitleRepository;
		this.siteRepository = siteRepository;
		this.mealTypeRepository = mealTypeRepository;
		this.transportTypeRepository = transportTypeRepository;
		this.platformStatusRepository = platformStatusRepository;
	}

	@Override
	public RequestEntityType supports() {
		return RequestEntityType.CONTRACT;
	}

	@Override
	public Long apply(Request request) {
		ContractPendingDataDto datos = deserializar(request.getPendingData());

		Contract contract = new Contract();
		contract.setStaff(staffRepository.findById(datos.getStaffId())
			.orElseThrow(() -> new ResourceNotFoundException("El colaborador indicado no existe.")));
		contract.setCompany(companyRepository.findById(datos.getCompanyId())
			.orElseThrow(() -> new ResourceNotFoundException("La empresa indicada no existe.")));
		contract.setContractType(contractTypeRepository.findById(datos.getContractTypeId())
			.orElseThrow(() -> new ResourceNotFoundException("El tipo de contrato indicado no existe.")));
		contract.setJobTitle(jobTitleRepository.findById(datos.getJobTitleId())
			.orElseThrow(() -> new ResourceNotFoundException("El cargo indicado no existe.")));
		contract.setSite(siteRepository.findById(datos.getSiteId())
			.orElseThrow(() -> new ResourceNotFoundException("La sucursal indicada no existe.")));
		contract.setProject(request.getProject());
		contract.setStartDate(datos.getStartDate());
		contract.setEndDate(datos.getEndDate());
		contract.setBaseSalary(datos.getBaseSalary());
		contract.setAgreedSalary(datos.getAgreedSalary());
		contract.setWeeklyWorkHours(datos.getWeeklyWorkHours());
		contract.setWorkDays(datos.getWorkDays());
		contract.setContractDetail(datos.getContractDetail());
		if (datos.getMealTypeId() != null) {
			contract.setMealType(mealTypeRepository.findById(datos.getMealTypeId())
				.orElseThrow(() -> new ResourceNotFoundException("El tipo de colacion indicado no existe.")));
		}
		if (datos.getTransportTypeId() != null) {
			contract.setTransportType(transportTypeRepository.findById(datos.getTransportTypeId())
				.orElseThrow(() -> new ResourceNotFoundException("El tipo de movilizacion indicado no existe.")));
		}
		contract.setStatus(resolveEstadoActivo());

		return contractRepository.save(contract).getId();
	}

	private PlatformStatus resolveEstadoActivo() {
		return platformStatusRepository.findBySubModuleAndCode(SUBMODULE_CONTRACT, CODE_ACTIVE)
			.orElseThrow(() -> new ResourceNotFoundException("El estado ACTIVE de contrato no está sembrado."));
	}

	private ContractPendingDataDto deserializar(String pendingData) {
		try {
			return objectMapper.readValue(pendingData, ContractPendingDataDto.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("El pendingData de la solicitud es invalido.", e);
		}
	}
}
