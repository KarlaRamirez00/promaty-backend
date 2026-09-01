package com.promaty.rrhh.services.client.business.validation;

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

import com.promaty.rrhh.dto.client.CreateClientDto;
import com.promaty.rrhh.dto.client.UpdateClientDto;
import com.promaty.rrhh.entity.Client;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ClientRepository;

@ExtendWith(MockitoExtension.class)
class ClientValidationTest {

	@Mock
	private ClientRepository clientRepository;

	@InjectMocks
	private ClientValidation clientValidation;

	@Test
	void validateCreate_conNombreDuplicado_lanzaErrorEnName() {
		when(clientRepository.findByName("Sodimac")).thenReturn(Optional.of(new Client()));

		assertThatThrownBy(() -> clientValidation.validateCreate(createDto("Sodimac")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateCreate_conNombreLibre_noLanzaExcepcion() {
		when(clientRepository.findByName("Sodimac")).thenReturn(Optional.empty());

		assertThatCode(() -> clientValidation.validateCreate(createDto("Sodimac")))
			.doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_conNombreDuplicadoDeOtroRegistro_lanzaErrorEnName() {
		Client otro = new Client();
		otro.setId(99L);
		when(clientRepository.findByName("Falabella")).thenReturn(Optional.of(otro));

		assertThatThrownBy(() -> clientValidation.validateUpdate(1L, updateDto("Falabella")))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("name"));
	}

	@Test
	void validateUpdate_conNombreDuplicadoDelMismoRegistro_noLanzaExcepcion() {
		Client mismo = new Client();
		mismo.setId(1L);
		when(clientRepository.findByName("Falabella")).thenReturn(Optional.of(mismo));

		assertThatCode(() -> clientValidation.validateUpdate(1L, updateDto("Falabella")))
			.doesNotThrowAnyException();
	}

	private CreateClientDto createDto(String name) {
		CreateClientDto dto = new CreateClientDto();
		dto.setName(name);
		return dto;
	}

	private UpdateClientDto updateDto(String name) {
		UpdateClientDto dto = new UpdateClientDto();
		dto.setName(name);
		return dto;
	}
}
