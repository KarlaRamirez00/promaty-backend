package com.promaty.rrhh.services.staff.business.validation;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.entity.AccountType;
import com.promaty.rrhh.entity.Bank;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.AfpRepository;
import com.promaty.rrhh.repository.BankRepository;
import com.promaty.rrhh.repository.ComunaRepository;
import com.promaty.rrhh.repository.EducationLevelRepository;
import com.promaty.rrhh.repository.HealthSystemRepository;
import com.promaty.rrhh.repository.MaritalStatusRepository;
import com.promaty.rrhh.repository.NationalityRepository;
import com.promaty.rrhh.repository.RegisteredSexRepository;
import com.promaty.rrhh.repository.StaffRepository;

@Component
public class StaffValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";
	private static final int EDAD_MINIMA = 18;
	private static final String CODIGO_BANCO_ESTADO = "BANCO_ESTADO";

	private final StaffRepository staffRepository;
	private final RegisteredSexRepository registeredSexRepository;
	private final MaritalStatusRepository maritalStatusRepository;
	private final NationalityRepository nationalityRepository;
	private final EducationLevelRepository educationLevelRepository;
	private final AfpRepository afpRepository;
	private final HealthSystemRepository healthSystemRepository;
	private final BankRepository bankRepository;
	private final ComunaRepository comunaRepository;

	public StaffValidation(
		StaffRepository staffRepository,
		RegisteredSexRepository registeredSexRepository,
		MaritalStatusRepository maritalStatusRepository,
		NationalityRepository nationalityRepository,
		EducationLevelRepository educationLevelRepository,
		AfpRepository afpRepository,
		HealthSystemRepository healthSystemRepository,
		BankRepository bankRepository,
		ComunaRepository comunaRepository
	) {
		this.staffRepository = staffRepository;
		this.registeredSexRepository = registeredSexRepository;
		this.maritalStatusRepository = maritalStatusRepository;
		this.nationalityRepository = nationalityRepository;
		this.educationLevelRepository = educationLevelRepository;
		this.afpRepository = afpRepository;
		this.healthSystemRepository = healthSystemRepository;
		this.bankRepository = bankRepository;
		this.comunaRepository = comunaRepository;
	}

	public void validateCreate(CreateStaffDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (staffRepository.findByIdentificationNumber(dto.getIdentificationNumber()).isPresent()) {
			errores.put("identificationNumber", "Ya existe un colaborador con este numero de identificacion.");
		}
		if (dto.getIdentificationType() == IdentificationType.RUT
			&& !esRutValido(dto.getIdentificationNumber())) {
			errores.put("identificationNumber", "El RUT ingresado no es valido.");
		}
		validarMayoriaDeEdad(dto.getBirthDate(), errores);
		validarRelaciones(
			errores,
			dto.getRegisteredSexId(),
			dto.getMaritalStatusId(),
			dto.getNationalityId(),
			dto.getEducationLevelId(),
			dto.getAfpId(),
			dto.getHealthSystemId(),
			dto.getBankId(),
			dto.getComunaId()
		);
		validarHijos(errores, dto.getHasChildren(), dto.getChildrenCount());
		validarTipoCuenta(errores, dto.getAccountType(), dto.getBankId());

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(UpdateStaffDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		validarMayoriaDeEdad(dto.getBirthDate(), errores);
		validarRelaciones(
			errores,
			dto.getRegisteredSexId(),
			dto.getMaritalStatusId(),
			dto.getNationalityId(),
			dto.getEducationLevelId(),
			dto.getAfpId(),
			dto.getHealthSystemId(),
			dto.getBankId(),
			dto.getComunaId()
		);
		validarHijos(errores, dto.getHasChildren(), dto.getChildrenCount());
		validarTipoCuenta(errores, dto.getAccountType(), dto.getBankId());

		lanzarSiHayErrores(errores);
	}

	private void validarMayoriaDeEdad(LocalDate birthDate, Map<String, String> errores) {
		if (birthDate != null && Period.between(birthDate, LocalDate.now()).getYears() < EDAD_MINIMA) {
			errores.put("birthDate", "El colaborador debe ser mayor de " + EDAD_MINIMA + " anios.");
		}
	}

	private void validarRelaciones(
		Map<String, String> errores,
		Long registeredSexId,
		Long maritalStatusId,
		Long nationalityId,
		Long educationLevelId,
		Long afpId,
		Long healthSystemId,
		Long bankId,
		Long comunaId
	) {
		if (registeredSexId != null && !registeredSexRepository.existsById(registeredSexId)) {
			errores.put("registeredSexId", "El sexo registral indicado no existe.");
		}
		if (maritalStatusId != null && !maritalStatusRepository.existsById(maritalStatusId)) {
			errores.put("maritalStatusId", "El estado civil indicado no existe.");
		}
		if (nationalityId != null && !nationalityRepository.existsById(nationalityId)) {
			errores.put("nationalityId", "La nacionalidad indicada no existe.");
		}
		if (educationLevelId != null && !educationLevelRepository.existsById(educationLevelId)) {
			errores.put("educationLevelId", "El nivel educacional indicado no existe.");
		}
		if (afpId != null && !afpRepository.existsById(afpId)) {
			errores.put("afpId", "La AFP indicada no existe.");
		}
		if (healthSystemId != null && !healthSystemRepository.existsById(healthSystemId)) {
			errores.put("healthSystemId", "El sistema de salud indicado no existe.");
		}
		if (bankId != null && !bankRepository.existsById(bankId)) {
			errores.put("bankId", "El banco indicado no existe.");
		}
		if (comunaId != null && !comunaRepository.existsById(comunaId)) {
			errores.put("comunaId", "La comuna indicada no existe.");
		}
	}

	private void validarHijos(Map<String, String> errores, Boolean hasChildren, Integer childrenCount) {
		if (Boolean.TRUE.equals(hasChildren) && childrenCount == null) {
			errores.put("childrenCount", "Debe indicar la cantidad de hijos.");
		}
	}

	private void validarTipoCuenta(Map<String, String> errores, AccountType accountType, Long bankId) {
		if (accountType != AccountType.RUT || bankId == null) {
			return;
		}
		Bank bank = bankRepository.findById(bankId).orElse(null);
		if (bank == null || !CODIGO_BANCO_ESTADO.equals(bank.getCode())) {
			errores.put("accountType", "El tipo de cuenta 'Cuenta Rut' solo es valido para BancoEstado.");
		}
	}

	private boolean esRutValido(String rut) {
		String limpio = rut.replace(".", "").replace("-", "").toUpperCase();
		if (limpio.length() < 2) {
			return false;
		}
		String cuerpo = limpio.substring(0, limpio.length() - 1);
		String digitoVerificador = limpio.substring(limpio.length() - 1);
		if (!cuerpo.chars().allMatch(Character::isDigit)) {
			return false;
		}

		int suma = 0;
		int multiplicador = 2;
		for (int i = cuerpo.length() - 1; i >= 0; i--) {
			suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
			multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
		}
		int resto = 11 - (suma % 11);
		String esperado = switch (resto) {
			case 11 -> "0";
			case 10 -> "K";
			default -> String.valueOf(resto);
		};
		return esperado.equals(digitoVerificador);
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
