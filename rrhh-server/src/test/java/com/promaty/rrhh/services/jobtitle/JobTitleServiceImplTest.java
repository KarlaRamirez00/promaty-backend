package com.promaty.rrhh.services.jobtitle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.JobTitle;
import com.promaty.rrhh.repository.JobTitleRepository;

@ExtendWith(MockitoExtension.class)
class JobTitleServiceImplTest {

	@Mock
	private JobTitleRepository jobTitleRepository;

	@InjectMocks
	private JobTitleServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		JobTitle albanil = new JobTitle();
		albanil.setId(1L);
		albanil.setName("Albañiles");
		albanil.setCode("7112");
		when(jobTitleRepository.findByActiveTrueOrderByName()).thenReturn(List.of(albanil));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("7112");
	}
}
