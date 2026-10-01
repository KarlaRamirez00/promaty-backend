package com.promaty.rrhh.services.region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.Region;
import com.promaty.rrhh.repository.RegionRepository;

@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {

	@Mock
	private RegionRepository regionRepository;

	@InjectMocks
	private RegionServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		Region rm = new Region();
		rm.setId(1L);
		rm.setName("Región Metropolitana de Santiago");
		rm.setCode("CL-RM");
		when(regionRepository.findByActiveTrueOrderByName()).thenReturn(List.of(rm));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("CL-RM");
	}
}
