package com.promaty.rrhh.services.holidayprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessDayCalculatorTest {

	@Mock
	private HolidayProvider holidayProvider;

	@InjectMocks
	private BusinessDayCalculator calculator;

	@Test
	void countBusinessDaysBetween_soloDiasDeSemana_cuentaCorrectamente() {
		when(holidayProvider.getHolidays(2026)).thenReturn(Set.of());

		// Lunes 2026-01-05 a viernes 2026-01-09: 4 dias habiles transcurridos (mar-vie)
		long dias = calculator.countBusinessDaysBetween(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 9));

		assertThat(dias).isEqualTo(4);
	}

	@Test
	void countBusinessDaysBetween_saltandoFinDeSemana_noCuentaSabadoNiDomingo() {
		when(holidayProvider.getHolidays(2026)).thenReturn(Set.of());

		// Viernes 2026-01-09 a lunes 2026-01-12: solo el lunes cuenta (sabado y domingo no)
		long dias = calculator.countBusinessDaysBetween(LocalDate.of(2026, 1, 9), LocalDate.of(2026, 1, 12));

		assertThat(dias).isEqualTo(1);
	}

	@Test
	void countBusinessDaysBetween_conFeriadoEnElRango_noLoCuenta() {
		LocalDate feriado = LocalDate.of(2026, 1, 7);
		when(holidayProvider.getHolidays(2026)).thenReturn(Set.of(feriado));

		long dias = calculator.countBusinessDaysBetween(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 9));

		assertThat(dias).isEqualTo(3);
	}

	@Test
	void countBusinessDaysBetween_conStartIgualOPosteriorAEnd_retornaCero() {
		long dias = calculator.countBusinessDaysBetween(LocalDate.of(2026, 1, 9), LocalDate.of(2026, 1, 5));

		assertThat(dias).isZero();
	}
}
