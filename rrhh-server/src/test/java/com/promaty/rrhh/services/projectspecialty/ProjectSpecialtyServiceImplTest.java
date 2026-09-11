package com.promaty.rrhh.services.projectspecialty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.projectspecialty.CreateProjectSpecialtyDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyDetailDto;
import com.promaty.rrhh.dto.projectspecialty.UpdateProjectSpecialtyDto;
import com.promaty.rrhh.entity.ProjectSpecialty;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;
import com.promaty.rrhh.services.projectspecialty.business.validation.ProjectSpecialtyValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;

@ExtendWith(MockitoExtension.class)
class ProjectSpecialtyServiceImplTest {

	@Mock
	private ProjectSpecialtyRepository projectSpecialtyRepository;

	@Mock
	private ProjectSpecialtyValidation projectSpecialtyValidation;

	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private ProjectSpecialtyServiceImpl service;

	@Test
	void createProjectSpecialty_conDatosValidos_validaGuardaYRetornaId() {
		CreateProjectSpecialtyDto dto = new CreateProjectSpecialtyDto();
		dto.setName("Eléctrica");
		when(projectSpecialtyRepository.save(any(ProjectSpecialty.class)))
			.thenReturn(projectSpecialty(7L, "Eléctrica", true));

		Long id = service.createProjectSpecialty(dto);

		assertThat(id).isEqualTo(7L);
		verify(projectSpecialtyValidation).validateCreate(dto);
	}

	@Test
	void updateProjectSpecialty_registroNoExiste_lanzaResourceNotFoundException() {
		UpdateProjectSpecialtyDto dto = new UpdateProjectSpecialtyDto();
		dto.setName("Eléctrica");
		when(projectSpecialtyRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateProjectSpecialty(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getProjectSpecialtyDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(projectSpecialtyRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getProjectSpecialtyDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void toggleProjectSpecialtyActive_registroActivo_loDesactivaYRetornaDetalle() {
		ProjectSpecialty existente = projectSpecialty(1L, "Eléctrica", true);
		when(projectSpecialtyRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(projectSpecialtyRepository.save(existente)).thenReturn(existente);

		ProjectSpecialtyDetailDto detalle = service.toggleProjectSpecialtyActive(1L);

		assertThat(existente.getActive()).isFalse();
		assertThat(detalle.getActive()).isFalse();
		assertThat(detalle.getName()).isEqualTo("Eléctrica");
	}

	private ProjectSpecialty projectSpecialty(Long id, String name, boolean active) {
		ProjectSpecialty entidad = new ProjectSpecialty();
		entidad.setId(id);
		entidad.setName(name);
		entidad.setActive(active);
		return entidad;
	}
}
