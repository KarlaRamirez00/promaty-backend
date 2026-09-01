package com.promaty.rrhh.services.projecttype.business.validation;

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

import com.promaty.rrhh.dto.projecttype.CreateProjectTypeDto;
import com.promaty.rrhh.dto.projecttype.UpdateProjectTypeDto;
import com.promaty.rrhh.entity.ProjectType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ProjectTypeRepository;

@ExtendWith(MockitoExtension.class)
class ProjectTypeValidationTest {

	@Mock
	private ProjectTypeRepository projectTypeRepository;

	@InjectMocks
	private ProjectTypeValidation projectTypeValidation;

	@Test
	void validateCreate_conNombreDuplicado_lanzaErrorEnName() {
		when(projectTypeRepository.findByName("Obra gruesa")).thenReturn(Optional.of(new ProjectType()));

		assertThatThrownBy(() -> projectTypeValidation.validateCreate(createDto("Obra gruesa")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateCreate_conNombreLibre_noLanzaExcepcion() {
		when(projectTypeRepository.findByName("Obra gruesa")).thenReturn(Optional.empty());

		assertThatCode(() -> projectTypeValidation.validateCreate(createDto("Obra gruesa")))
			.doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_conNombreDuplicadoDeOtroRegistro_lanzaErrorEnName() {
		ProjectType otro = new ProjectType();
		otro.setId(99L);
		when(projectTypeRepository.findByName("Terminaciones")).thenReturn(Optional.of(otro));

		assertThatThrownBy(() -> projectTypeValidation.validateUpdate(1L, updateDto("Terminaciones")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateUpdate_conNombreDuplicadoDelMismoRegistro_noLanzaExcepcion() {
		ProjectType mismo = new ProjectType();
		mismo.setId(1L);
		when(projectTypeRepository.findByName("Terminaciones")).thenReturn(Optional.of(mismo));

		assertThatCode(() -> projectTypeValidation.validateUpdate(1L, updateDto("Terminaciones")))
			.doesNotThrowAnyException();
	}

	private CreateProjectTypeDto createDto(String name) {
		CreateProjectTypeDto dto = new CreateProjectTypeDto();
		dto.setName(name);
		return dto;
	}

	private UpdateProjectTypeDto updateDto(String name) {
		UpdateProjectTypeDto dto = new UpdateProjectTypeDto();
		dto.setName(name);
		return dto;
	}
}
