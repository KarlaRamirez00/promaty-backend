package com.promaty.rrhh.services.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.project.CreateProjectDto;
import com.promaty.rrhh.dto.project.ProjectDetailDto;
import com.promaty.rrhh.dto.project.UpdateProjectDto;
import com.promaty.rrhh.dto.project.UpdateProjectStatusDto;
import com.promaty.rrhh.entity.Client;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.ProjectSpecialty;
import com.promaty.rrhh.entity.ProjectType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.services.project.business.builder.CreateProjectBuilder;
import com.promaty.rrhh.services.project.business.builder.ProjectRelationsResolver;
import com.promaty.rrhh.services.project.business.builder.UpdateProjectBuilder;
import com.promaty.rrhh.services.project.business.validation.ProjectValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private ProjectValidation projectValidation;
	@Mock
	private CreateProjectBuilder createProjectBuilder;
	@Mock
	private UpdateProjectBuilder updateProjectBuilder;
	@Mock
	private ProjectRelationsResolver relationsResolver;
	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private ProjectServiceImpl service;

	@Test
	void createProject_conDatosValidos_validaConstruyeGuardaYRetornaId() {
		CreateProjectDto dto = new CreateProjectDto();
		Project construido = new Project();
		Project guardado = projectConId(5L);
		when(createProjectBuilder.build(dto)).thenReturn(construido);
		when(projectRepository.save(construido)).thenReturn(guardado);

		Long id = service.createProject(dto);

		assertThat(id).isEqualTo(5L);
		verify(projectValidation).validateCreate(dto);
	}

	@Test
	void updateProject_registroNoExiste_lanzaResourceNotFoundException() {
		UpdateProjectDto dto = new UpdateProjectDto();
		when(projectRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateProject(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void updateProject_conDatosValidos_validaAplicaBuilderYGuarda() {
		UpdateProjectDto dto = new UpdateProjectDto();
		Project existente = projectConId(1L);
		when(projectRepository.findById(1L)).thenReturn(Optional.of(existente));

		service.updateProject(1L, dto);

		verify(projectValidation).validateUpdate(1L, dto);
		verify(updateProjectBuilder).apply(existente, dto);
		verify(projectRepository).save(existente);
	}

	@Test
	void updateProjectStatus_registroNoExiste_lanzaResourceNotFoundException() {
		UpdateProjectStatusDto dto = new UpdateProjectStatusDto();
		dto.setStatusId(40L);
		when(projectRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateProjectStatus(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void updateProjectStatus_conDatosValidos_validaResuelveEstadoYGuarda() {
		UpdateProjectStatusDto dto = new UpdateProjectStatusDto();
		dto.setStatusId(40L);
		Project existente = projectCompleto();
		PlatformStatus nuevoEstado = new PlatformStatus();
		nuevoEstado.setId(40L);
		nuevoEstado.setCode("IN_PROGRESS");
		when(projectRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(relationsResolver.resolveStatus(40L)).thenReturn(nuevoEstado);
		when(projectRepository.save(existente)).thenReturn(existente);

		ProjectDetailDto detalle = service.updateProjectStatus(1L, dto);

		verify(projectValidation).validateStatusChange(dto);
		assertThat(existente.getStatus()).isEqualTo(nuevoEstado);
		assertThat(detalle.getStatus().getCode()).isEqualTo("IN_PROGRESS");
	}

	@Test
	void getProjectDetail_registroNoExiste_lanzaResourceNotFoundException() {
		when(projectRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getProjectDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getProjectDetail_registroExiste_retornaDetalleMapeadoConSusRelaciones() {
		when(projectRepository.findById(1L)).thenReturn(Optional.of(projectCompleto()));

		ProjectDetailDto detalle = service.getProjectDetail(1L);

		assertThat(detalle.getName()).isEqualTo("Edificio Norte");
		assertThat(detalle.getType().getName()).isEqualTo("Obra gruesa");
		assertThat(detalle.getClient().getName()).isEqualTo("Sodimac");
		assertThat(detalle.getStatus().getCode()).isEqualTo("IN_PROGRESS");
	}

	private Project projectConId(Long id) {
		Project project = new Project();
		project.setId(id);
		return project;
	}

	private Project projectCompleto() {
		Project project = projectConId(1L);
		project.setName("Edificio Norte");
		project.setCostCenterCode("00824");
		project.setStartDate(LocalDate.of(2026, 1, 1));

		ProjectType type = new ProjectType();
		type.setId(10L);
		type.setName("Obra gruesa");
		project.setType(type);

		ProjectSpecialty specialty = new ProjectSpecialty();
		specialty.setId(20L);
		specialty.setName("Eléctrica");
		project.setSpecialty(specialty);

		Client client = new Client();
		client.setId(30L);
		client.setName("Sodimac");
		project.setClient(client);

		PlatformStatus status = new PlatformStatus();
		status.setId(40L);
		status.setCode("IN_PROGRESS");
		status.setName("En ejecución");
		project.setStatus(status);

		return project;
	}
}
