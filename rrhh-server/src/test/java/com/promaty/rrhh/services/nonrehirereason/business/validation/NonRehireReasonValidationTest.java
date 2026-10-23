package com.promaty.rrhh.services.nonrehirereason.business.validation;

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

import com.promaty.rrhh.dto.nonrehirereason.CreateNonRehireReasonDto;
import com.promaty.rrhh.dto.nonrehirereason.UpdateNonRehireReasonDto;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.NonRehireReasonRepository;

@ExtendWith(MockitoExtension.class)
class NonRehireReasonValidationTest {

	@Mock
	private NonRehireReasonRepository nonRehireReasonRepository;

	@InjectMocks
	private NonRehireReasonValidation nonRehireReasonValidation;

	@Test
	void validateCreate_conNombreDuplicado_lanzaErrorEnName() {
		NonRehireReason existente = new NonRehireReason();
		existente.setId(5L);
		when(nonRehireReasonRepository.findByName("Bajo desempeño")).thenReturn(Optional.of(existente));

		assertThatThrownBy(() -> nonRehireReasonValidation.validateCreate(createDto("Bajo desempeño", "LOW_PERFORMANCE")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateCreate_conCodigoDuplicado_lanzaErrorEnCode() {
		NonRehireReason existente = new NonRehireReason();
		existente.setId(5L);
		when(nonRehireReasonRepository.findByCode("LOW_PERFORMANCE")).thenReturn(Optional.of(existente));

		assertThatThrownBy(() -> nonRehireReasonValidation.validateCreate(createDto("Otro nombre", "LOW_PERFORMANCE")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("code"));
	}

	@Test
	void validateCreate_sinDuplicados_noLanzaExcepcion() {
		when(nonRehireReasonRepository.findByName("Bajo desempeño")).thenReturn(Optional.empty());
		when(nonRehireReasonRepository.findByCode("LOW_PERFORMANCE")).thenReturn(Optional.empty());

		assertThatCode(() -> nonRehireReasonValidation.validateCreate(createDto("Bajo desempeño", "LOW_PERFORMANCE")))
			.doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_conNombreDuplicadoDeOtroRegistro_lanzaErrorEnName() {
		NonRehireReason otro = new NonRehireReason();
		otro.setId(99L);
		when(nonRehireReasonRepository.findByName("Falta de compromiso")).thenReturn(Optional.of(otro));
		when(nonRehireReasonRepository.findByCode("LACK_OF_COMMITMENT")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> nonRehireReasonValidation.validateUpdate(1L, updateDto("Falta de compromiso", "LACK_OF_COMMITMENT")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateUpdate_conNombreYCodigoDelMismoRegistro_noLanzaExcepcion() {
		NonRehireReason mismo = new NonRehireReason();
		mismo.setId(1L);
		when(nonRehireReasonRepository.findByName("Falta de compromiso")).thenReturn(Optional.of(mismo));
		when(nonRehireReasonRepository.findByCode("LACK_OF_COMMITMENT")).thenReturn(Optional.of(mismo));

		assertThatCode(() -> nonRehireReasonValidation.validateUpdate(1L, updateDto("Falta de compromiso", "LACK_OF_COMMITMENT")))
			.doesNotThrowAnyException();
	}

	private CreateNonRehireReasonDto createDto(String name, String code) {
		CreateNonRehireReasonDto dto = new CreateNonRehireReasonDto();
		dto.setName(name);
		dto.setCode(code);
		return dto;
	}

	private UpdateNonRehireReasonDto updateDto(String name, String code) {
		UpdateNonRehireReasonDto dto = new UpdateNonRehireReasonDto();
		dto.setName(name);
		dto.setCode(code);
		return dto;
	}
}
