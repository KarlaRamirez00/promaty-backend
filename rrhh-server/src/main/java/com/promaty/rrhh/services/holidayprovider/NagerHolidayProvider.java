package com.promaty.rrhh.services.holidayprovider;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NagerHolidayProvider implements HolidayProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(NagerHolidayProvider.class);
	private static final String URL_BASE = "https://date.nager.at/api/v3/PublicHolidays/{year}/CL";

	private final RestClient restClient;
	private final Map<Integer, Set<LocalDate>> cachePorAno = new ConcurrentHashMap<>();

	public NagerHolidayProvider(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.build();
	}

	@Override
	public Set<LocalDate> getHolidays(int year) {
		return cachePorAno.computeIfAbsent(year, this::consultarFeriados);
	}

	private Set<LocalDate> consultarFeriados(int year) {
		try {
			NagerHolidayDto[] feriados = restClient.get()
				.uri(URL_BASE, year)
				.retrieve()
				.body(NagerHolidayDto[].class);
			if (feriados == null) {
				return Set.of();
			}
			return Arrays.stream(feriados)
				.map(NagerHolidayDto::date)
				.collect(Collectors.toUnmodifiableSet());
		} catch (RestClientException e) {
			LOGGER.warn("No se pudo obtener los feriados {} desde Nager.Date, se calculará solo con fin de semana.",
				year, e);
			return Set.of();
		}
	}
}
