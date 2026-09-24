package com.promaty.rrhh.services.colaborador.business.validation;

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

import com.promaty.rrhh.dto.colaborador.CreateColaboradorDto;
import com.promaty.rrhh.dto.colaborador.UpdateColaboradorDto;
import com.promaty.rrhh.entity.Colaborador;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ColaboradorRepository;

@ExtendWith(MockitoExtension.class)
class ColaboradorValidationTest {

	@Mock
	private ColaboradorRepository colaboradorRepository;

	@InjectMocks
	private ColaboradorValidation colaboradorValidation;

	@Test
	void validateCreate_conRutValidoYDatosValidos_noLanzaExcepcion() {
		when(colaboradorRepository.findByIdentificationNumber("12345678-5")).thenReturn(Optional.empty());

		assertThatCode(() -> colaboradorValidation.validateCreate(createDto("12345678-5")))
			.doesNotThrowAnyException();
	}

	@Test
	void validateCreate_conRutConDigitoVerificadorInvalido_lanzaErrorEnIdentificationNumber() {
		when(colaboradorRepository.findByIdentificationNumber("12345678-9")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> colaboradorValidation.validateCreate(createDto("12345678-9")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("identificationNumber"));
	}

	@Test
	void validateCreate_conIdentificacionDuplicada_lanzaErrorEnIdentificationNumber() {
		when(colaboradorRepository.findByIdentificationNumber("12345678-5"))
			.thenReturn(Optional.of(new Colaborador()));

		assertThatThrownBy(() -> colaboradorValidation.validateCreate(createDto("12345678-5")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("identificationNumber"));
	}

	@Test
	void validateCreate_conMenorDeEdad_lanzaErrorEnBirthDate() {
		when(colaboradorRepository.findByIdentificationNumber("12345678-5")).thenReturn(Optional.empty());
		CreateColaboradorDto dto = createDto("12345678-5");
		dto.setBirthDate(LocalDate.now().minusYears(10));

		assertThatThrownBy(() -> colaboradorValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("birthDate"));
	}

	@Test
	void validateCreate_conPasaporteYFormatoNoNumerico_noValidaComoRut() {
		when(colaboradorRepository.findByIdentificationNumber("AB123456")).thenReturn(Optional.empty());
		CreateColaboradorDto dto = createDto("AB123456");
		dto.setIdentificationType(IdentificationType.PASAPORTE);

		assertThatCode(() -> colaboradorValidation.validateCreate(dto)).doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_conMenorDeEdad_lanzaErrorEnBirthDate() {
		UpdateColaboradorDto dto = updateDto();
		dto.setBirthDate(LocalDate.now().minusYears(5));

		assertThatThrownBy(() -> colaboradorValidation.validateUpdate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("birthDate"));
	}

	@Test
	void validateUpdate_conDatosValidos_noLanzaExcepcion() {
		assertThatCode(() -> colaboradorValidation.validateUpdate(updateDto())).doesNotThrowAnyException();
	}

	private CreateColaboradorDto createDto(String identificationNumber) {
		CreateColaboradorDto dto = new CreateColaboradorDto();
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

	private UpdateColaboradorDto updateDto() {
		UpdateColaboradorDto dto = new UpdateColaboradorDto();
		dto.setFirstName("Juan");
		dto.setPaternalLastName("Perez");
		dto.setMaternalLastName("Soto");
		dto.setBirthDate(LocalDate.of(1990, 1, 1));
		dto.setPersonalEmail("juan.perez@example.com");
		dto.setPhone1("912345678");
		return dto;
	}
}
