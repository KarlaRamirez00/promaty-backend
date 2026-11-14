package com.promaty.rrhh.services.annex.business.change;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.JobTitle;
import com.promaty.rrhh.repository.JobTitleRepository;

@ExtendWith(MockitoExtension.class)
class JobTitleChangeRuleTest {

	@Mock
	private JobTitleRepository jobTitleRepository;

	@InjectMocks
	private JobTitleChangeRule rule;

	@Test
	void validate_cargoActivoYDistintoDelActual_noAgregaErrores() {
		when(jobTitleRepository.findById(2L)).thenReturn(Optional.of(cargo(2L, true)));

		assertThat(validar(2L)).isEmpty();
	}

	@Test
	void validate_mismoCargoQueElActual_rechazaElCambio() {
		when(jobTitleRepository.findById(1L)).thenReturn(Optional.of(cargo(1L, true)));

		assertThat(validar(1L)).containsEntry("newJobTitleId", "El nuevo cargo debe ser distinto del actual.");
	}

	@Test
	void validate_cargoInactivo_rechazaElCambio() {
		when(jobTitleRepository.findById(2L)).thenReturn(Optional.of(cargo(2L, false)));

		assertThat(validar(2L)).containsEntry("newJobTitleId", "El cargo indicado no está activo.");
	}

	@Test
	void validate_sinCargo_loExigeComoObligatorio() {
		assertThat(validar(null)).containsEntry("newJobTitleId", "El nuevo cargo es obligatorio.");
	}

	@Test
	void apply_guardaElCargoAnteriorYActualizaElContrato() {
		JobTitle actual = cargo(1L, true);
		JobTitle nuevo = cargo(2L, true);
		Contract contract = new Contract();
		contract.setJobTitle(actual);
		Annex annex = new Annex();
		annex.setNewJobTitle(nuevo);

		rule.apply(annex, contract);

		assertThat(annex.getPreviousJobTitle()).isEqualTo(actual);
		assertThat(contract.getJobTitle()).isEqualTo(nuevo);
	}

	private Map<String, String> validar(Long nuevoCargoId) {
		UpdateAnnexDto dto = new UpdateAnnexDto();
		dto.setNewJobTitleId(nuevoCargoId);
		Contract contract = new Contract();
		contract.setJobTitle(cargo(1L, true));
		Map<String, String> errores = new LinkedHashMap<>();
		rule.validate(dto, contract, errores);
		return errores;
	}

	private JobTitle cargo(Long id, boolean active) {
		JobTitle jobTitle = new JobTitle();
		jobTitle.setId(id);
		jobTitle.setActive(active);
		return jobTitle;
	}
}
