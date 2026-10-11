package com.promaty.rrhh.services.request.handler;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.rrhh.dto.request.ContractPendingDataDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.ContractType;
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
import com.promaty.rrhh.services.holidayprovider.BusinessDayCalculator;
import com.promaty.rrhh.services.rexplus.RexPlusClient;
import com.promaty.rrhh.services.rexplus.RexSyncResult;

/**
 * RequestValidation ya confirmó que las FK de contractData existen al crear la Request; para cuando
 * este Handler corre (después de 2 niveles de aprobación) pueden haber pasado días, de ahí el
 * orElseThrow defensivo en cada resolución.
 */
@Component
public class ContractRequestHandler implements RequestHandler {

	private static final String SUBMODULE_CONTRACT = "contract";
	private static final String CODE_ACTIVE = "ACTIVE";
	private static final String CODE_SYNC_ERROR = "SYNC_ERROR";
	private static final String CODE_LATE_REGISTRATION = "LATE_REGISTRATION";
	private static final String CODE_CONTRACT_TYPE_OBRA = "O";
	private static final int PLAZO_HABIL_OBRA = 5;
	private static final int PLAZO_HABIL_GENERAL = 15;

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
	private final RexPlusClient rexPlusClient;
	private final BusinessDayCalculator businessDayCalculator;

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
		PlatformStatusRepository platformStatusRepository,
		RexPlusClient rexPlusClient,
		BusinessDayCalculator businessDayCalculator
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
		this.rexPlusClient = rexPlusClient;
		this.businessDayCalculator = businessDayCalculator;
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
		ContractType contractType = contractTypeRepository.findById(datos.getContractTypeId())
			.orElseThrow(() -> new ResourceNotFoundException("El tipo de contrato indicado no existe."));
		contract.setContractType(contractType);
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
		if (superoPlazoLegal(contract.getStartDate(), contractType)) {
			contract.setStatus(resolveEstado(CODE_LATE_REGISTRATION));
			return contractRepository.save(contract).getId();
		}

		RexSyncResult sincronizacion = rexPlusClient.syncContract(contract);
		if (sincronizacion.success()) {
			contract.setContractNumber(sincronizacion.externalContractNumber());
			contract.setStatus(resolveEstado(CODE_ACTIVE));
		} else {
			contract.setStatus(resolveEstado(CODE_SYNC_ERROR));
		}

		return contractRepository.save(contract).getId();
	}

	private boolean superoPlazoLegal(LocalDate startDate, ContractType contractType) {
		long diasHabilesTranscurridos = businessDayCalculator.countBusinessDaysBetween(startDate, LocalDate.now());
		int plazoMaximo = CODE_CONTRACT_TYPE_OBRA.equals(contractType.getCode()) ? PLAZO_HABIL_OBRA : PLAZO_HABIL_GENERAL;
		return diasHabilesTranscurridos > plazoMaximo;
	}

	private PlatformStatus resolveEstado(String code) {
		return platformStatusRepository.findBySubModuleAndCode(SUBMODULE_CONTRACT, code)
			.orElseThrow(() -> new ResourceNotFoundException("El estado " + code + " de contrato no está sembrado."));
	}

	private ContractPendingDataDto deserializar(String pendingData) {
		try {
			return objectMapper.readValue(pendingData, ContractPendingDataDto.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("El pendingData de la solicitud es invalido.", e);
		}
	}
}
