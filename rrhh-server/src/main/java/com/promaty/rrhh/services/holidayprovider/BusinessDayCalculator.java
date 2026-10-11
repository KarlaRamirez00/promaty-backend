package com.promaty.rrhh.services.holidayprovider;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class BusinessDayCalculator {

	private final HolidayProvider holidayProvider;

	public BusinessDayCalculator(HolidayProvider holidayProvider) {
		this.holidayProvider = holidayProvider;
	}

	public long countBusinessDaysBetween(LocalDate start, LocalDate end) {
		if (start == null || end == null || !start.isBefore(end)) {
			return 0;
		}
		long dias = 0;
		for (LocalDate fecha = start.plusDays(1); !fecha.isAfter(end); fecha = fecha.plusDays(1)) {
			if (esDiaHabil(fecha)) {
				dias++;
			}
		}
		return dias;
	}

	private boolean esDiaHabil(LocalDate fecha) {
		boolean esFinDeSemana = fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY;
		return !esFinDeSemana && !holidayProvider.getHolidays(fecha.getYear()).contains(fecha);
	}
}
