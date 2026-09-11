package com.promaty.rrhh.services.projecttype;

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

import com.promaty.rrhh.dto.projecttype.CreateProjectTypeDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeDetailDto;
import com.promaty.rrhh.dto.projecttype.UpdateProjectTypeDto;
import com.promaty.rrhh.entity.ProjectType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ProjectTypeRepository;
import com.promaty.rrhh.services.projecttype.business.validation.ProjectTypeValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;

@ExtendWith(MockitoExtension.class)
class ProjectTypeServiceImplTest {

	@Mock
	private ProjectTypeRepository projectTypeRepository;

	@Mock
	private ProjectTypeValidation projectTypeValidation;

	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private ProjectTypeServiceImpl service;

	@Test
	void createProjectType_conDatosValidos_validaGuardaYRetornaId() {
		CreateProjectTypeDto dto = new CreateProjectTypeDto();
		dto.setName("Obra gruesa");
		when(projectTypeRepository.save(any(ProjectType.class))).thenReturn(projectType(7L, "Obra gruesa", true));

		Long id = service.createProjectType(dto);

		assertThat(id).isEqualTo(7L);
		verify(projectTypeValidation).validateCreate(dto);
	}

	@Test
	void updateProjectType_registroNoExiste_lanzaResourceNotFoundException() {
		UpdateProjectTypeDto dto = new UpdateProjectTypeDto();
		dto.setName("Obra gruesa");
		when(projectTypeRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateProjectType(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getProjectTypeDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(projectTypeRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getProjectTypeDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void toggleProjectTypeActive_registroActivo_loDesactivaYRetornaDetalle() {
		ProjectType existente = projectType(1L, "Obra gruesa", true);
		when(projectTypeRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(projectTypeRepository.save(existente)).thenReturn(existente);

		ProjectTypeDetailDto detalle = service.toggleProjectTypeActive(1L);

		assertThat(existente.getActive()).isFalse();
		assertThat(detalle.getActive()).isFalse();
		assertThat(detalle.getName()).isEqualTo("Obra gruesa");
	}

	private ProjectType projectType(Long id, String name, boolean active) {
		ProjectType entidad = new ProjectType();
		entidad.setId(id);
		entidad.setName(name);
		entidad.setActive(active);
		return entidad;
	}
}
