package com.promaty.rrhh.services.comuna;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.Comuna;
import com.promaty.rrhh.repository.ComunaRepository;

@ExtendWith(MockitoExtension.class)
class ComunaServiceImplTest {

	@Mock
	private ComunaRepository comunaRepository;

	@InjectMocks
	private ComunaServiceImpl service;

	@Test
	void listOptionsByProvincia_retornaSoloActivasDeEsaProvincia() {
		Comuna santiago = new Comuna();
		santiago.setId(1L);
		santiago.setName("Santiago");
		santiago.setCode("rm0101");
		when(comunaRepository.findByProvinciaIdAndActiveTrueOrderByName(9L)).thenReturn(List.of(santiago));

		List<CatalogOptionDto> opciones = service.listOptionsByProvincia(9L);

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("rm0101");
	}
}
