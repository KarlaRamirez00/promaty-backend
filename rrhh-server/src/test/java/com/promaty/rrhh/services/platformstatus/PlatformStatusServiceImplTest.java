package com.promaty.rrhh.services.platformstatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.repository.PlatformStatusRepository;

@ExtendWith(MockitoExtension.class)
class PlatformStatusServiceImplTest {

	@Mock
	private PlatformStatusRepository platformStatusRepository;

	@InjectMocks
	private PlatformStatusServiceImpl service;

	@Test
	void listOptionsBySubModule_mapeaCadaEntidadYPreservaElOrdenDelRepositorio() {
		when(platformStatusRepository.findBySubModuleAndActiveTrueOrderBySortOrder("project"))
			.thenReturn(List.of(
				platformStatus(1L, "PLANNED", "Planificado"),
				platformStatus(2L, "IN_PROGRESS", "En ejecución")
			));

		List<PlatformStatusOptionDto> opciones = service.listOptionsBySubModule("project");

		assertThat(opciones).extracting(PlatformStatusOptionDto::getCode)
			.containsExactly("PLANNED", "IN_PROGRESS");
		assertThat(opciones.get(0).getId()).isEqualTo(1L);
		assertThat(opciones.get(0).getName()).isEqualTo("Planificado");
	}

	@Test
	void listOptionsBySubModule_sinEstados_retornaListaVacia() {
		when(platformStatusRepository.findBySubModuleAndActiveTrueOrderBySortOrder("contract"))
			.thenReturn(List.of());

		assertThat(service.listOptionsBySubModule("contract")).isEmpty();
	}

	private PlatformStatus platformStatus(Long id, String code, String name) {
		PlatformStatus estado = new PlatformStatus();
		estado.setId(id);
		estado.setCode(code);
		estado.setName(name);
		return estado;
	}
}
