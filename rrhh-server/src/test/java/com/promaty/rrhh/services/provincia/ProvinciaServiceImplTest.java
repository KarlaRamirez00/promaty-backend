package com.promaty.rrhh.services.provincia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.Provincia;
import com.promaty.rrhh.repository.ProvinciaRepository;

@ExtendWith(MockitoExtension.class)
class ProvinciaServiceImplTest {

	@Mock
	private ProvinciaRepository provinciaRepository;

	@InjectMocks
	private ProvinciaServiceImpl service;

	@Test
	void listOptionsByRegion_retornaSoloActivasDeEsaRegion() {
		Provincia santiago = new Provincia();
		santiago.setId(1L);
		santiago.setName("Santiago");
		santiago.setCode("rm01");
		when(provinciaRepository.findByRegionIdAndActiveTrueOrderByName(8L)).thenReturn(List.of(santiago));

		List<CatalogOptionDto> opciones = service.listOptionsByRegion(8L);

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("rm01");
	}
}
