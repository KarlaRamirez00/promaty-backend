package com.promaty.rrhh.services.healthsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.HealthSystem;
import com.promaty.rrhh.repository.HealthSystemRepository;

@ExtendWith(MockitoExtension.class)
class HealthSystemServiceImplTest {

	@Mock
	private HealthSystemRepository healthSystemRepository;

	@InjectMocks
	private HealthSystemServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		HealthSystem fonasa = new HealthSystem();
		fonasa.setId(1L);
		fonasa.setName("Fonasa");
		fonasa.setCode("FONASA");
		when(healthSystemRepository.findByActiveTrueOrderByName()).thenReturn(List.of(fonasa));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("FONASA");
	}
}
