package com.promaty.rrhh.services.contracttype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.ContractType;
import com.promaty.rrhh.repository.ContractTypeRepository;

@ExtendWith(MockitoExtension.class)
class ContractTypeServiceImplTest {

	@Mock
	private ContractTypeRepository contractTypeRepository;

	@InjectMocks
	private ContractTypeServiceImpl service;

	@Test
	void listOptions_retornaSoloActivosMapeadosAOptionDto() {
		ContractType faena = new ContractType();
		faena.setId(1L);
		faena.setName("Contrato por obra o faena");
		faena.setCode("F");
		when(contractTypeRepository.findByActiveTrueOrderByName()).thenReturn(List.of(faena));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("F");
	}
}
