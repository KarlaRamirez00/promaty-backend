package com.promaty.rrhh.services.nationality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.Nationality;
import com.promaty.rrhh.repository.NationalityRepository;

@ExtendWith(MockitoExtension.class)
class NationalityServiceImplTest {

	@Mock
	private NationalityRepository nationalityRepository;

	@InjectMocks
	private NationalityServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		Nationality chilena = new Nationality();
		chilena.setId(1L);
		chilena.setName("Chilena");
		chilena.setCode("CHL");
		when(nationalityRepository.findByActiveTrueOrderByName()).thenReturn(List.of(chilena));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("CHL");
	}
}
