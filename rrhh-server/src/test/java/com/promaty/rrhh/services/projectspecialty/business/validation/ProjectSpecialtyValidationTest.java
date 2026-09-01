package com.promaty.rrhh.services.projectspecialty.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.projectspecialty.CreateProjectSpecialtyDto;
import com.promaty.rrhh.dto.projectspecialty.UpdateProjectSpecialtyDto;
import com.promaty.rrhh.entity.ProjectSpecialty;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;

@ExtendWith(MockitoExtension.class)
class ProjectSpecialtyValidationTest {

	@Mock
	private ProjectSpecialtyRepository projectSpecialtyRepository;

	@InjectMocks
	private ProjectSpecialtyValidation projectSpecialtyValidation;

	@Test
	void validateCreate_conNombreDuplicado_lanzaErrorEnName() {
		when(projectSpecialtyRepository.findByName("Eléctrica")).thenReturn(Optional.of(new ProjectSpecialty()));

		assertThatThrownBy(() -> projectSpecialtyValidation.validateCreate(createDto("Eléctrica")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateCreate_conNombreLibre_noLanzaExcepcion() {
		when(projectSpecialtyRepository.findByName("Eléctrica")).thenReturn(Optional.empty());

		assertThatCode(() -> projectSpecialtyValidation.validateCreate(createDto("Eléctrica")))
			.doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_conNombreDuplicadoDeOtroRegistro_lanzaErrorEnName() {
		ProjectSpecialty otro = new ProjectSpecialty();
		otro.setId(99L);
		when(projectSpecialtyRepository.findByName("Sanitaria")).thenReturn(Optional.of(otro));

		assertThatThrownBy(() -> projectSpecialtyValidation.validateUpdate(1L, updateDto("Sanitaria")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateUpdate_conNombreDuplicadoDelMismoRegistro_noLanzaExcepcion() {
		ProjectSpecialty mismo = new ProjectSpecialty();
		mismo.setId(1L);
		when(projectSpecialtyRepository.findByName("Sanitaria")).thenReturn(Optional.of(mismo));

		assertThatCode(() -> projectSpecialtyValidation.validateUpdate(1L, updateDto("Sanitaria")))
			.doesNotThrowAnyException();
	}

	private CreateProjectSpecialtyDto createDto(String name) {
		CreateProjectSpecialtyDto dto = new CreateProjectSpecialtyDto();
		dto.setName(name);
		return dto;
	}

	private UpdateProjectSpecialtyDto updateDto(String name) {
		UpdateProjectSpecialtyDto dto = new UpdateProjectSpecialtyDto();
		dto.setName(name);
		return dto;
	}
}
