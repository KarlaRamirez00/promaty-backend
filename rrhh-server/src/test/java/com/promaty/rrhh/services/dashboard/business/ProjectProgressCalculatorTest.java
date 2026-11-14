package com.promaty.rrhh.services.dashboard.business;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.promaty.rrhh.dto.dashboard.ProjectProgressDto;
import com.promaty.rrhh.entity.Project;

class ProjectProgressCalculatorTest {

	private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
	private static final LocalDate TERMINO = LocalDate.of(2026, 4, 11);

	@Test
	void calculate_aMitadDelPlazo_retornaPorcentajeTranscurridoYDiasRestantes() {
		ProjectProgressDto avance = ProjectProgressCalculator.calculate(proyecto(INICIO, TERMINO), LocalDate.of(2026, 2, 20));

		assertThat(avance.getElapsedPercent()).isEqualTo(50);
		assertThat(avance.getDaysRemaining()).isEqualTo(50);
		assertThat(avance.getOverdueDays()).isZero();
	}

	@Test
	void calculate_antesDeLaFechaDeInicio_retornaCeroPorCiento() {
		ProjectProgressDto avance = ProjectProgressCalculator.calculate(proyecto(INICIO, TERMINO), LocalDate.of(2025, 12, 15));

		assertThat(avance.getElapsedPercent()).isZero();
	}

	@Test
	void calculate_conPlazoVencido_retornaCienPorCientoYDiasDeAtraso() {
		ProjectProgressDto avance = ProjectProgressCalculator.calculate(proyecto(INICIO, TERMINO), LocalDate.of(2026, 4, 21));

		assertThat(avance.getElapsedPercent()).isEqualTo(100);
		assertThat(avance.getDaysRemaining()).isZero();
		assertThat(avance.getOverdueDays()).isEqualTo(10);
	}

	@Test
	void calculate_sinFechaDeTermino_dejaPorcentajeYDiasEnNull() {
		ProjectProgressDto avance = ProjectProgressCalculator.calculate(proyecto(INICIO, null), LocalDate.of(2026, 2, 20));

		assertThat(avance.getElapsedPercent()).isNull();
		assertThat(avance.getDaysRemaining()).isNull();
		assertThat(avance.getOverdueDays()).isNull();
	}

	@Test
	void calculate_conInicioYTerminoElMismoDia_noDividePorCero() {
		ProjectProgressDto avance = ProjectProgressCalculator.calculate(proyecto(INICIO, INICIO), INICIO.plusDays(1));

		assertThat(avance.getElapsedPercent()).isEqualTo(100);
	}

	private Project proyecto(LocalDate inicio, LocalDate termino) {
		Project project = new Project();
		project.setId(1L);
		project.setName("Edificio Central");
		project.setCostCenterCode("00824");
		project.setStartDate(inicio);
		project.setEndDate(termino);
		return project;
	}
}
