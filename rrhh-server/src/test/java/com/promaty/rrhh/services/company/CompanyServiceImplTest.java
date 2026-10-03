package com.promaty.rrhh.services.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.entity.Company;
import com.promaty.rrhh.repository.CompanyRepository;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

	@Mock
	private CompanyRepository companyRepository;

	@InjectMocks
	private CompanyServiceImpl service;

	@Test
	void listOptions_retornaSoloActivasMapeadasAOptionDto() {
		Company promaty = new Company();
		promaty.setId(1L);
		promaty.setName("Constructora Promaty S.A.");
		promaty.setCode("PROMATY");
		when(companyRepository.findByActiveTrueOrderByName()).thenReturn(List.of(promaty));

		List<CatalogOptionDto> opciones = service.listOptions();

		assertThat(opciones).hasSize(1);
		assertThat(opciones.get(0).getCode()).isEqualTo("PROMATY");
	}
}
