package com.promaty.rrhh.services.educationlevel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.EducationLevel;
import com.promaty.rrhh.repository.EducationLevelRepository;

@ExtendWith(MockitoExtension.class)
class EducationLevelServiceImplTest {

	@Mock
	private EducationLevelRepository educationLevelRepository;

	@InjectMocks
	private EducationLevelServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		EducationLevel media = new EducationLevel();
		media.setId(1L);
		media.setName("Educación media");
		media.setCode("HIGH_SCHOOL");
		when(educationLevelRepository.findByActiveTrueOrderByName()).thenReturn(List.of(media));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("HIGH_SCHOOL");
	}
}
