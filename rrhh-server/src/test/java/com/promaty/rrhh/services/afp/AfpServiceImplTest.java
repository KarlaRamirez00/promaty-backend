package com.promaty.rrhh.services.afp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.Afp;
import com.promaty.rrhh.repository.AfpRepository;

@ExtendWith(MockitoExtension.class)
class AfpServiceImplTest {

	@Mock
	private AfpRepository afpRepository;

	@InjectMocks
	private AfpServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		Afp capital = new Afp();
		capital.setId(1L);
		capital.setName("AFP Capital");
		capital.setCode("CAPITAL");
		when(afpRepository.findByActiveTrueOrderByName()).thenReturn(List.of(capital));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("CAPITAL");
	}
}
