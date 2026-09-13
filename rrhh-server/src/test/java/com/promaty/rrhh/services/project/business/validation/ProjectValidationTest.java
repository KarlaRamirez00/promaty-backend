package com.promaty.rrhh.services.project.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.project.CreateProjectDto;
import com.promaty.rrhh.dto.project.UpdateProjectDto;
import com.promaty.rrhh.dto.project.UpdateProjectStatusDto;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ClientRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.ProjectSpecialtyRepository;
import com.promaty.rrhh.repository.ProjectTypeRepository;

@ExtendWith(MockitoExtension.class)
class ProjectValidationTest {

	@Mock
	private ProjectRepository projectRepository;
	@Mock
	private ProjectTypeRepository projectTypeRepository;
	@Mock
	private ProjectSpecialtyRepository projectSpecialtyRepository;
	@Mock
	private ClientRepository clientRepository;
	@Mock
	private PlatformStatusRepository platformStatusRepository;

	@InjectMocks
	private ProjectValidation projectValidation;

	@Test
	void validateCreate_conCentroCostoDuplicado_lanzaErrorEnCostCenterCode() {
		todasLasFkExisten();
		when(projectRepository.findByCostCenterCode("00824")).thenReturn(Optional.of(new Project()));

		assertThatThrownBy(() -> projectValidation.validateCreate(createDto()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("costCenterCode"));
	}

	@Test
	void validateCreate_conFkInexistentes_acumulaUnErrorPorCadaRelacion() {
		when(projectRepository.findByCostCenterCode("00824")).thenReturn(Optional.empty());
		when(projectTypeRepository.existsById(10L)).thenReturn(false);
		when(projectSpecialtyRepository.existsById(20L)).thenReturn(false);
		when(clientRepository.existsById(30L)).thenReturn(false);

		assertThatThrownBy(() -> projectValidation.validateCreate(createDto()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKeys("typeId", "specialtyId", "clientId"));
	}

	@Test
	void validateCreate_conEndDateAnteriorAStartDate_lanzaErrorEnEndDate() {
		todasLasFkExisten();
		when(projectRepository.findByCostCenterCode("00824")).thenReturn(Optional.empty());
		CreateProjectDto dto = createDto();
		dto.setStartDate(LocalDate.of(2026, 3, 1));
		dto.setEndDate(LocalDate.of(2026, 2, 1));

		assertThatThrownBy(() -> projectValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("endDate"));
	}

	@Test
	void validateCreate_conDatosValidos_noLanzaExcepcion() {
		todasLasFkExisten();
		when(projectRepository.findByCostCenterCode("00824")).thenReturn(Optional.empty());

		assertThatCode(() -> projectValidation.validateCreate(createDto())).doesNotThrowAnyException();
	}

	@Test
	void validateCreate_conVariosErrores_losAcumulaTodos() {
		when(projectRepository.findByCostCenterCode("00824")).thenReturn(Optional.of(new Project()));
		when(projectTypeRepository.existsById(10L)).thenReturn(false);
		lenient().when(projectSpecialtyRepository.existsById(20L)).thenReturn(true);
		lenient().when(clientRepository.existsById(30L)).thenReturn(true);
		CreateProjectDto dto = createDto();
		dto.setStartDate(LocalDate.of(2026, 3, 1));
		dto.setEndDate(LocalDate.of(2026, 2, 1));

		assertThatThrownBy(() -> projectValidation.validateCreate(dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKeys("costCenterCode", "typeId", "endDate"));
	}

	@Test
	void validateUpdate_conCentroCostoDeOtroProyecto_lanzaErrorEnCostCenterCode() {
		todasLasFkExisten();
		Project otro = new Project();
		otro.setId(99L);
		when(projectRepository.findByCostCenterCode("00824")).thenReturn(Optional.of(otro));

		assertThatThrownBy(() -> projectValidation.validateUpdate(1L, updateDto()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("costCenterCode"));
	}

	@Test
	void validateUpdate_conCentroCostoDelMismoProyecto_noLanzaExcepcion() {
		todasLasFkExisten();
		Project mismo = new Project();
		mismo.setId(1L);
		when(projectRepository.findByCostCenterCode("00824")).thenReturn(Optional.of(mismo));

		assertThatCode(() -> projectValidation.validateUpdate(1L, updateDto())).doesNotThrowAnyException();
	}

	@Test
	void validateStatusChange_conEstadoInexistente_lanzaErrorEnStatusId() {
		when(platformStatusRepository.existsById(40L)).thenReturn(false);

		assertThatThrownBy(() -> projectValidation.validateStatusChange(statusDto(40L)))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("statusId"));
	}

	@Test
	void validateStatusChange_conEstadoExistente_noLanzaExcepcion() {
		when(platformStatusRepository.existsById(40L)).thenReturn(true);

		assertThatCode(() -> projectValidation.validateStatusChange(statusDto(40L))).doesNotThrowAnyException();
	}

	private void todasLasFkExisten() {
		lenient().when(projectTypeRepository.existsById(10L)).thenReturn(true);
		lenient().when(projectSpecialtyRepository.existsById(20L)).thenReturn(true);
		lenient().when(clientRepository.existsById(30L)).thenReturn(true);
	}

	private CreateProjectDto createDto() {
		CreateProjectDto dto = new CreateProjectDto();
		dto.setName("Edificio Norte");
		dto.setCostCenterCode("00824");
		dto.setTypeId(10L);
		dto.setSpecialtyId(20L);
		dto.setClientId(30L);
		dto.setStartDate(LocalDate.of(2026, 1, 1));
		return dto;
	}

	private UpdateProjectDto updateDto() {
		UpdateProjectDto dto = new UpdateProjectDto();
		dto.setName("Edificio Norte");
		dto.setCostCenterCode("00824");
		dto.setTypeId(10L);
		dto.setSpecialtyId(20L);
		dto.setClientId(30L);
		dto.setStartDate(LocalDate.of(2026, 1, 1));
		return dto;
	}

	private UpdateProjectStatusDto statusDto(Long statusId) {
		UpdateProjectStatusDto dto = new UpdateProjectStatusDto();
		dto.setStatusId(statusId);
		return dto;
	}
}
