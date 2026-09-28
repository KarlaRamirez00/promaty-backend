package com.promaty.rrhh.services.registeredsex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.RegisteredSex;
import com.promaty.rrhh.repository.RegisteredSexRepository;

@ExtendWith(MockitoExtension.class)
class RegisteredSexServiceImplTest {

	@Mock
	private RegisteredSexRepository registeredSexRepository;

	@InjectMocks
	private RegisteredSexServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		RegisteredSex masculino = new RegisteredSex();
		masculino.setId(1L);
		masculino.setName("Masculino");
		masculino.setCode("MALE");
		when(registeredSexRepository.findByActiveTrueOrderByName()).thenReturn(List.of(masculino));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("MALE");
	}
}
