package com.promaty.rrhh.services.legalterminationcause;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;

@ExtendWith(MockitoExtension.class)
class LegalTerminationCauseServiceImplTest {

	@Mock
	private LegalTerminationCauseRepository legalTerminationCauseRepository;

	@InjectMocks
	private LegalTerminationCauseServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		LegalTerminationCause renuncia = new LegalTerminationCause();
		renuncia.setId(1L);
		renuncia.setName("Renuncia voluntaria");
		renuncia.setCode("RESIGNATION");
		when(legalTerminationCauseRepository.findByActiveTrueOrderByName()).thenReturn(List.of(renuncia));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("RESIGNATION");
	}
}
