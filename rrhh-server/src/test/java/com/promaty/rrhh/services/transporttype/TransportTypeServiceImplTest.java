package com.promaty.rrhh.services.transporttype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.TransportType;
import com.promaty.rrhh.repository.TransportTypeRepository;

@ExtendWith(MockitoExtension.class)
class TransportTypeServiceImplTest {

	@Mock
	private TransportTypeRepository transportTypeRepository;

	@InjectMocks
	private TransportTypeServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		TransportType ninguna = new TransportType();
		ninguna.setId(1L);
		ninguna.setName("Sin movilización");
		ninguna.setCode("NONE");
		when(transportTypeRepository.findByActiveTrueOrderByName()).thenReturn(List.of(ninguna));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("NONE");
	}
}
