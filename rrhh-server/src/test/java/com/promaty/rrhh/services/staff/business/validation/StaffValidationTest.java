package com.promaty.rrhh.services.staff.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.StaffRepository;

@ExtendWith(MockitoExtension.class)
class StaffValidationTest {

	@Mock
	private StaffRepository staffRepository;

	@InjectMocks
	private StaffValidation staffValidation;

	@Test
	void validateCreate_conRutValidoYDatosValidos_noLanzaExcepcion() {
		when(staffRepository.findByIdentificationNumber("12345678-5")).thenReturn(Optional.empty());

		assertThatCode(() -> staffValidation.validateCreate(createDto("12345678-5")))
			.doesNotThrowAnyException();
	}

	@Test
	void validateCreate_conRutConDigitoVerificadorInvalido_lanzaErrorEnIdentificationNumber() {
		when(staffRepository.findByIdentificationNumber("12345678-9")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> staffValidation.validateCreate(createDto("12345678-9")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("identificationNumber"));
	}

	@Test
	void validateCreate_conIdentificacionDuplicada_lanzaErrorEnIdentificationNumber() {
		when(staffRepository.findByIdentificationNumber("12345678-5"))
			.thenReturn(Optional.of(new Staff()));

		assertThatThrownBy(() -> staffValidation.validateCreate(createDto("12345678-5")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("identificationNumber"));
	}

	@Test
	void validateCreate_conMenorDeEdad_lanzaErrorEnBirthDate() {
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
		when(staffRepository.findByIdentificationNumber("AB123456")).thenReturn(Optional.empty());
		CreateStaffDto dto = createDto("AB123456");
		dto.setIdentificationType(IdentificationType.PASSPORT);

		assertThatCode(() -> staffValidation.validateCreate(dto)).doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_conMenorDeEdad_lanzaErrorEnBirthDate() {
		UpdateStaffDto dto = updateDto();
		dto.setBirthDate(LocalDate.now().minusYears(5));

		assertThatThrownBy(() -> staffValidation.validateUpdate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("birthDate"));
	}

	@Test
	void validateUpdate_conDatosValidos_noLanzaExcepcion() {
		assertThatCode(() -> staffValidation.validateUpdate(updateDto())).doesNotThrowAnyException();
	}

	private CreateStaffDto createDto(String identificationNumber) {
		CreateStaffDto dto = new CreateStaffDto();
		dto.setIdentificationType(IdentificationType.RUT);
		dto.setIdentificationNumber(identificationNumber);
		dto.setFirstName("Juan");
		dto.setPaternalLastName("Perez");
		dto.setMaternalLastName("Soto");
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		dto.setPersonalEmail("juan.perez@example.com");
		dto.setPhone1("912345678");
		return dto;
	}

	private UpdateStaffDto updateDto() {
		UpdateStaffDto dto = new UpdateStaffDto();
		dto.setFirstName("Juan");
		dto.setPaternalLastName("Perez");
		dto.setMaternalLastName("Soto");
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		dto.setPersonalEmail("juan.perez@example.com");
		dto.setPhone1("912345678");
		return dto;
	}
}
