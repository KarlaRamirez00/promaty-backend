package com.promaty.rrhh.services.staff.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.entity.AccountType;
import com.promaty.rrhh.entity.Bank;
import com.promaty.rrhh.entity.ClothingSize;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.AfpRepository;
import com.promaty.rrhh.repository.BankRepository;
import com.promaty.rrhh.repository.EducationLevelRepository;
import com.promaty.rrhh.repository.HealthSystemRepository;
import com.promaty.rrhh.repository.MaritalStatusRepository;
import com.promaty.rrhh.repository.NationalityRepository;
import com.promaty.rrhh.repository.RegisteredSexRepository;
import com.promaty.rrhh.repository.StaffRepository;

@ExtendWith(MockitoExtension.class)
class StaffValidationTest {

	private static final Long BANK_ID = 40L;
	private static final Long BANCO_ESTADO_ID = 41L;

	@Mock
	private StaffRepository staffRepository;
	@Mock
	private RegisteredSexRepository registeredSexRepository;
	@Mock
	private MaritalStatusRepository maritalStatusRepository;
	@Mock
	private NationalityRepository nationalityRepository;
	@Mock
	private EducationLevelRepository educationLevelRepository;
	@Mock
	private AfpRepository afpRepository;
	@Mock
	private HealthSystemRepository healthSystemRepository;
	@Mock
	private BankRepository bankRepository;

	@InjectMocks
	private StaffValidation staffValidation;

	@Test
	void validateCreate_conRutValidoYDatosValidos_noLanzaExcepcion() {
		todasLasFkExisten();
		when(staffRepository.findByIdentificationNumber("12345678-5")).thenReturn(Optional.empty());

		assertThatCode(() -> staffValidation.validateCreate(createDto("12345678-5")))
			.doesNotThrowAnyException();
	}

	@Test
	void validateCreate_conRutConDigitoVerificadorInvalido_lanzaErrorEnIdentificationNumber() {
		todasLasFkExisten();
		when(staffRepository.findByIdentificationNumber("12345678-9")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> staffValidation.validateCreate(createDto("12345678-9")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("identificationNumber"));
	}

	@Test
	void validateCreate_conIdentificacionDuplicada_lanzaErrorEnIdentificationNumber() {
		todasLasFkExisten();
		when(staffRepository.findByIdentificationNumber("12345678-5"))
			.thenReturn(Optional.of(new Staff()));

		assertThatThrownBy(() -> staffValidation.validateCreate(createDto("12345678-5")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("identificationNumber"));
	}

	@Test
	void validateCreate_conMenorDeEdad_lanzaErrorEnBirthDate() {
		todasLasFkExisten();
		when(staffRepository.findByIdentificationNumber("12345678-5")).thenReturn(Optional.empty());
		CreateStaffDto dto = createDto("12345678-5");
		dto.setBirthDate(LocalDate.now().minusYears(10));

		assertThatThrownBy(() -> staffValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("birthDate"));
	}

	@Test
	void validateCreate_conPasaporteYFormatoNoNumerico_noValidaComoRut() {
		todasLasFkExisten();
		when(staffRepository.findByIdentificationNumber("AB123456")).thenReturn(Optional.empty());
		CreateStaffDto dto = createDto("AB123456");
		dto.setIdentificationType(IdentificationType.PASSPORT);

		assertThatCode(() -> staffValidation.validateCreate(dto)).doesNotThrowAnyException();
	}

	@Test
	void validateCreate_conFkInexistentes_acumulaUnErrorPorCadaRelacion() {
		when(staffRepository.findByIdentificationNumber("12345678-5")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> staffValidation.validateCreate(createDto("12345678-5")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKeys(
					"registeredSexId", "maritalStatusId", "nationalityId",
					"educationLevelId", "afpId", "healthSystemId", "bankId"
				));
	}

	@Test
	void validateCreate_conHijosSinCantidad_lanzaErrorEnChildrenCount() {
		todasLasFkExisten();
		when(staffRepository.findByIdentificationNumber("12345678-5")).thenReturn(Optional.empty());
		CreateStaffDto dto = createDto("12345678-5");
		dto.setHasChildren(true);
		dto.setChildrenCount(null);

		assertThatThrownBy(() -> staffValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("childrenCount"));
	}

	@Test
	void validateCreate_conCuentaRutYBancoDistintoDeBancoEstado_lanzaErrorEnAccountType() {
		todasLasFkExisten();
		when(staffRepository.findByIdentificationNumber("12345678-5")).thenReturn(Optional.empty());
		CreateStaffDto dto = createDto("12345678-5");
		dto.setAccountType(AccountType.RUT);

		assertThatThrownBy(() -> staffValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("accountType"));
	}

	@Test
	void validateCreate_conCuentaRutYBancoEstado_noLanzaExcepcion() {
		todasLasFkExisten();
		when(staffRepository.findByIdentificationNumber("12345678-5")).thenReturn(Optional.empty());
		Bank bancoEstado = new Bank();
		bancoEstado.setCode("BANCO_ESTADO");
		when(bankRepository.findById(BANCO_ESTADO_ID)).thenReturn(Optional.of(bancoEstado));
		when(bankRepository.existsById(BANCO_ESTADO_ID)).thenReturn(true);
		CreateStaffDto dto = createDto("12345678-5");
		dto.setAccountType(AccountType.RUT);
		dto.setBankId(BANCO_ESTADO_ID);

		assertThatCode(() -> staffValidation.validateCreate(dto)).doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_conMenorDeEdad_lanzaErrorEnBirthDate() {
		todasLasFkExisten();
		UpdateStaffDto dto = updateDto();
		dto.setBirthDate(LocalDate.now().minusYears(5));

		assertThatThrownBy(() -> staffValidation.validateUpdate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("birthDate"));
	}

	@Test
	void validateUpdate_conDatosValidos_noLanzaExcepcion() {
		todasLasFkExisten();
		assertThatCode(() -> staffValidation.validateUpdate(updateDto())).doesNotThrowAnyException();
	}

	private void todasLasFkExisten() {
		lenient().when(registeredSexRepository.existsById(1L)).thenReturn(true);
		lenient().when(maritalStatusRepository.existsById(2L)).thenReturn(true);
		lenient().when(nationalityRepository.existsById(3L)).thenReturn(true);
		lenient().when(educationLevelRepository.existsById(4L)).thenReturn(true);
		lenient().when(afpRepository.existsById(5L)).thenReturn(true);
		lenient().when(healthSystemRepository.existsById(6L)).thenReturn(true);
		lenient().when(bankRepository.existsById(BANK_ID)).thenReturn(true);
	}

	private CreateStaffDto createDto(String identificationNumber) {
		CreateStaffDto dto = new CreateStaffDto();
		dto.setIdentificationType(IdentificationType.RUT);
		dto.setIdentificationNumber(identificationNumber);
		dto.setFirstName("Juan");
		dto.setPaternalLastName("Perez");
		dto.setMaternalLastName("Soto");
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		dto.setRegisteredSexId(1L);
		dto.setMaritalStatusId(2L);
		dto.setNationalityId(3L);
		dto.setPhone1("912345678");
		dto.setEmergencyPhone("987654321");
		dto.setEmergencyContactName("Maria Perez");
		dto.setAddress("Calle Falsa 123");
		dto.setCity("Santiago");
		dto.setHasChildren(false);
		dto.setPersonalEmail("juan.perez@example.com");
		dto.setShoeSize(42);
		dto.setClothingSize(ClothingSize.M);
		dto.setEducationLevelId(4L);
		dto.setAfpId(5L);
		dto.setHealthSystemId(6L);
		dto.setBankId(BANK_ID);
		dto.setAccountType(AccountType.CHECKING);
		dto.setAccountNumber("00012345678");
		return dto;
	}

	private UpdateStaffDto updateDto() {
		UpdateStaffDto dto = new UpdateStaffDto();
		dto.setFirstName("Juan");
		dto.setPaternalLastName("Perez");
		dto.setMaternalLastName("Soto");
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		dto.setRegisteredSexId(1L);
		dto.setMaritalStatusId(2L);
		dto.setNationalityId(3L);
		dto.setPhone1("912345678");
		dto.setEmergencyPhone("987654321");
		dto.setEmergencyContactName("Maria Perez");
		dto.setAddress("Calle Falsa 123");
		dto.setCity("Santiago");
		dto.setHasChildren(false);
		dto.setPersonalEmail("juan.perez@example.com");
		dto.setShoeSize(42);
		dto.setClothingSize(ClothingSize.M);
		dto.setEducationLevelId(4L);
		dto.setAfpId(5L);
		dto.setHealthSystemId(6L);
		dto.setBankId(BANK_ID);
		dto.setAccountType(AccountType.CHECKING);
		dto.setAccountNumber("00012345678");
		return dto;
	}
}
