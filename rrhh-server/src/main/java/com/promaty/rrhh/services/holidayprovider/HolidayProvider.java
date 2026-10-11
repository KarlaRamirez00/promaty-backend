package com.promaty.rrhh.services.holidayprovider;

import java.time.LocalDate;
import java.util.Set;

public interface HolidayProvider {

	Set<LocalDate> getHolidays(int year);
}
