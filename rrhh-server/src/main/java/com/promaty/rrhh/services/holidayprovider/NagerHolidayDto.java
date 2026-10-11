package com.promaty.rrhh.services.holidayprovider;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NagerHolidayDto(LocalDate date) {
}
