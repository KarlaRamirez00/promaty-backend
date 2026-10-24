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

		assertThatThrownBy(() -> nonRehireReasonValidation.validateCreate(createDto("Bajo desempeño")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateCreate_sinDuplicados_noLanzaExcepcion() {
		when(nonRehireReasonRepository.findByName("Bajo desempeño")).thenReturn(Optional.empty());

		assertThatCode(() -> nonRehireReasonValidation.validateCreate(createDto("Bajo desempeño")))
			.doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_conNombreDuplicadoDeOtroRegistro_lanzaErrorEnName() {
		NonRehireReason otro = new NonRehireReason();
		otro.setId(99L);
		when(nonRehireReasonRepository.findByName("Falta de compromiso")).thenReturn(Optional.of(otro));

		assertThatThrownBy(() -> nonRehireReasonValidation.validateUpdate(1L, updateDto("Falta de compromiso")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateUpdate_conNombreDelMismoRegistro_noLanzaExcepcion() {
		NonRehireReason mismo = new NonRehireReason();
		mismo.setId(1L);
		when(nonRehireReasonRepository.findByName("Falta de compromiso")).thenReturn(Optional.of(mismo));

		assertThatCode(() -> nonRehireReasonValidation.validateUpdate(1L, updateDto("Falta de compromiso")))
			.doesNotThrowAnyException();
	}

	@Test
	void resolveCode_conNombreSinColision_generaSlugDesdeElNombre() {
		when(nonRehireReasonRepository.findByCode("FALTA_COMPROMISO")).thenReturn(Optional.empty());

		String code = nonRehireReasonValidation.resolveCode("Falta de compromiso", null);

		assertThat(code).isEqualTo("FALTA_COMPROMISO");
	}

	@Test
	void resolveCode_conCodigoYaUsadoPorOtroRegistro_agregaSufijoNumerico() {
		NonRehireReason otro = new NonRehireReason();
		otro.setId(99L);
		when(nonRehireReasonRepository.findByCode("FALTA_COMPROMISO")).thenReturn(Optional.of(otro));
		when(nonRehireReasonRepository.findByCode("FALTA_COMPROMISO_2")).thenReturn(Optional.empty());

		String code = nonRehireReasonValidation.resolveCode("Falta de compromiso", null);

		assertThat(code).isEqualTo("FALTA_COMPROMISO_2");
	}

	@Test
	void resolveCode_conCodigoUsadoPorElMismoRegistro_noLoConsideraColision() {
		NonRehireReason mismo = new NonRehireReason();
		mismo.setId(1L);
		when(nonRehireReasonRepository.findByCode("FALTA_COMPROMISO")).thenReturn(Optional.of(mismo));

		String code = nonRehireReasonValidation.resolveCode("Falta de compromiso", 1L);

		assertThat(code).isEqualTo("FALTA_COMPROMISO");
	}

	private CreateNonRehireReasonDto createDto(String name) {
		CreateNonRehireReasonDto dto = new CreateNonRehireReasonDto();
		dto.setName(name);
		return dto;
	}

	private UpdateNonRehireReasonDto updateDto(String name) {
		UpdateNonRehireReasonDto dto = new UpdateNonRehireReasonDto();
		dto.setName(name);
		return dto;
	}
}
