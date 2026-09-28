package com.promaty.rrhh.services.maritalstatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.MaritalStatus;
import com.promaty.rrhh.repository.MaritalStatusRepository;

@ExtendWith(MockitoExtension.class)
class MaritalStatusServiceImplTest {

	@Mock
	private MaritalStatusRepository maritalStatusRepository;

	@InjectMocks
	private MaritalStatusServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		MaritalStatus soltero = new MaritalStatus();
		soltero.setId(1L);
		soltero.setName("Soltero/a");
		soltero.setCode("SINGLE");
		when(maritalStatusRepository.findByActiveTrueOrderByName()).thenReturn(List.of(soltero));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("SINGLE");
	}
}
