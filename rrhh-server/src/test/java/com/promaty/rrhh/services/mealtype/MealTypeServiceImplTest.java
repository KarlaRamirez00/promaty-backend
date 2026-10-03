package com.promaty.rrhh.services.mealtype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.MealType;
import com.promaty.rrhh.repository.MealTypeRepository;

@ExtendWith(MockitoExtension.class)
class MealTypeServiceImplTest {

	@Mock
	private MealTypeRepository mealTypeRepository;

	@InjectMocks
	private MealTypeServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		MealType efectivo = new MealType();
		efectivo.setId(1L);
		efectivo.setName("Colación en efectivo");
		efectivo.setCode("CASH");
		when(mealTypeRepository.findByActiveTrueOrderByName()).thenReturn(List.of(efectivo));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("CASH");
	}
}
