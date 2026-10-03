package com.promaty.rrhh.services.site;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.Site;
import com.promaty.rrhh.repository.SiteRepository;

@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

	@Mock
	private SiteRepository siteRepository;

	@InjectMocks
	private SiteServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		Site matriz = new Site();
		matriz.setId(1L);
		matriz.setName("Casa matriz");
		matriz.setCode("MATRIZ");
		when(siteRepository.findByActiveTrueOrderByName()).thenReturn(List.of(matriz));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("MATRIZ");
	}
}
