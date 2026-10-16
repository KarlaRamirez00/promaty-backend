package com.promaty.rrhh.services.staff.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Afp;
import com.promaty.rrhh.entity.Bank;
import com.promaty.rrhh.entity.Comuna;
import com.promaty.rrhh.entity.EducationLevel;
import com.promaty.rrhh.entity.HealthSystem;
import com.promaty.rrhh.entity.MaritalStatus;
import com.promaty.rrhh.entity.Nationality;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.RegisteredSex;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.AfpRepository;
import com.promaty.rrhh.repository.BankRepository;
import com.promaty.rrhh.repository.ComunaRepository;
import com.promaty.rrhh.repository.EducationLevelRepository;
import com.promaty.rrhh.repository.HealthSystemRepository;
import com.promaty.rrhh.repository.MaritalStatusRepository;
import com.promaty.rrhh.repository.NationalityRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.RegisteredSexRepository;

/**
 * StaffValidation ya confirma que las 8 FK existen antes de llegar acá; el orElseThrow es una
 * defensa ante la carrera entre esa validación y este guardado, no una repetición de esa validación.
 */
@Component
public class StaffRelationsResolver {

	private static final String SUB_MODULE_STAFF = "staff";
	private static final String CODIGO_ESTADO_INICIAL = "PENDING_CONTRACT";

	private final RegisteredSexRepository registeredSexRepository;
	private final MaritalStatusRepository maritalStatusRepository;
	private final NationalityRepository nationalityRepository;
	private final EducationLevelRepository educationLevelRepository;
	private final AfpRepository afpRepository;
	private final HealthSystemRepository healthSystemRepository;
	private final BankRepository bankRepository;
	private final ComunaRepository comunaRepository;
	private final PlatformStatusRepository platformStatusRepository;

	public StaffRelationsResolver(
		RegisteredSexRepository registeredSexRepository,
		MaritalStatusRepository maritalStatusRepository,
		NationalityRepository nationalityRepository,
		EducationLevelRepository educationLevelRepository,
		AfpRepository afpRepository,
		HealthSystemRepository healthSystemRepository,
		BankRepository bankRepository,
		ComunaRepository comunaRepository,
		PlatformStatusRepository platformStatusRepository
	) {
		this.registeredSexRepository = registeredSexRepository;
		this.maritalStatusRepository = maritalStatusRepository;
		this.nationalityRepository = nationalityRepository;
		this.educationLevelRepository = educationLevelRepository;
		this.afpRepository = afpRepository;
		this.healthSystemRepository = healthSystemRepository;
		this.bankRepository = bankRepository;
		this.comunaRepository = comunaRepository;
		this.platformStatusRepository = platformStatusRepository;
	}

	public RegisteredSex resolveRegisteredSex(Long registeredSexId) {
		return registeredSexRepository.findById(registeredSexId)
			.orElseThrow(() -> new ResourceNotFoundException("El sexo registral indicado no existe."));
	}

	public MaritalStatus resolveMaritalStatus(Long maritalStatusId) {
		return maritalStatusRepository.findById(maritalStatusId)
			.orElseThrow(() -> new ResourceNotFoundException("El estado civil indicado no existe."));
	}

	public Nationality resolveNationality(Long nationalityId) {
		return nationalityRepository.findById(nationalityId)
			.orElseThrow(() -> new ResourceNotFoundException("La nacionalidad indicada no existe."));
	}

	public EducationLevel resolveEducationLevel(Long educationLevelId) {
		return educationLevelRepository.findById(educationLevelId)
			.orElseThrow(() -> new ResourceNotFoundException("El nivel educacional indicado no existe."));
	}

	public Afp resolveAfp(Long afpId) {
		return afpRepository.findById(afpId)
			.orElseThrow(() -> new ResourceNotFoundException("La AFP indicada no existe."));
	}

	public HealthSystem resolveHealthSystem(Long healthSystemId) {
		return healthSystemRepository.findById(healthSystemId)
			.orElseThrow(() -> new ResourceNotFoundException("El sistema de salud indicado no existe."));
	}

	public Bank resolveBank(Long bankId) {
		return bankRepository.findById(bankId)
			.orElseThrow(() -> new ResourceNotFoundException("El banco indicado no existe."));
	}

	public Comuna resolveComuna(Long comunaId) {
		return comunaRepository.findById(comunaId)
			.orElseThrow(() -> new ResourceNotFoundException("La comuna indicada no existe."));
	}

	public PlatformStatus resolveStatus(Long statusId) {
		return platformStatusRepository.findById(statusId)
			.orElseThrow(() -> new ResourceNotFoundException("El estado indicado no existe."));
	}

	public PlatformStatus resolveDefaultStatus() {
		return platformStatusRepository.findBySubModuleAndCode(SUB_MODULE_STAFF, CODIGO_ESTADO_INICIAL)
			.orElseThrow(() -> new ResourceNotFoundException("No existe el estado inicial de colaborador (PENDING_CONTRACT)."));
	}
}
