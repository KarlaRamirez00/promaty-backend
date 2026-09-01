package com.promaty.rrhh.services.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.client.ClientDetailDto;
import com.promaty.rrhh.dto.client.CreateClientDto;
import com.promaty.rrhh.dto.client.UpdateClientDto;
import com.promaty.rrhh.entity.Client;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ClientRepository;
import com.promaty.rrhh.services.client.business.validation.ClientValidation;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private ClientValidation clientValidation;

	@InjectMocks
	private ClientServiceImpl service;

	@Test
	void createClient_conDatosValidos_validaGuardaYRetornaId() {
		CreateClientDto dto = new CreateClientDto();
		dto.setName("Sodimac");
		when(clientRepository.save(any(Client.class))).thenReturn(client(7L, "Sodimac", true));

		Long id = service.createClient(dto);

		assertThat(id).isEqualTo(7L);
		verify(clientValidation).validateCreate(dto);
	}

	@Test
	void updateClient_registroNoExiste_lanzaResourceNotFoundException() {
		UpdateClientDto dto = new UpdateClientDto();
		dto.setName("Sodimac");
		when(clientRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateClient(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getClientDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(clientRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getClientDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void toggleClientActive_registroActivo_loDesactivaYRetornaDetalle() {
		Client existente = client(1L, "Sodimac", true);
		when(clientRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(clientRepository.save(existente)).thenReturn(existente);

		ClientDetailDto detalle = service.toggleClientActive(1L);

		assertThat(existente.getActive()).isFalse();
		assertThat(detalle.getActive()).isFalse();
		assertThat(detalle.getName()).isEqualTo("Sodimac");
	}

	private Client client(Long id, String name, boolean active) {
		Client entidad = new Client();
		entidad.setId(id);
		entidad.setName(name);
		entidad.setActive(active);
		return entidad;
	}
}
