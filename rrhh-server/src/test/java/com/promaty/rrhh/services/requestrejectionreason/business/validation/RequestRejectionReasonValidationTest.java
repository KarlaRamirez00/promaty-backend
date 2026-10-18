package com.promaty.rrhh.services.requestrejectionreason.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.requestrejectionreason.CreateRequestRejectionReasonDto;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;

@ExtendWith(MockitoExtension.class)
class RequestRejectionReasonValidationTest {

	@Mock
	private RequestRejectionReasonRepository requestRejectionReasonRepository;

	@InjectMocks
	private RequestRejectionReasonValidation validation;

	@Test
	void validateCreate_conSubModuloInexistente_lanzaErrorEnSubModule() {
		CreateRequestRejectionReasonDto dto = new CreateRequestRejectionReasonDto();
		dto.setName("Renuncia voluntaria");
		dto.setSubModule("finiquitos");
		when(requestRejectionReasonRepository.findByName(dto.getName())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> validation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields()).containsKey("subModule"));
	}

	@Test
	void validateCreate_conSubModuloValido_noLanzaExcepcion() {
		CreateRequestRejectionReasonDto dto = new CreateRequestRejectionReasonDto();
		dto.setName("Documentacion incompleta");
		dto.setSubModule("contracts");
		when(requestRejectionReasonRepository.findByName(dto.getName())).thenReturn(Optional.empty());

		assertThatNoException().isThrownBy(() -> validation.validateCreate(dto));
	}
}
