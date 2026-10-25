package com.promaty.rrhh.services.termination.business.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.services.holidayprovider.BusinessDayCalculator;

@ExtendWith(MockitoExtension.class)
class TerminationAnnulmentValidationTest {

	@Mock
	private BusinessDayCalculator businessDayCalculator;

	@InjectMocks
	private TerminationAnnulmentValidation validation;

	@Test
	void validateAnnul_conStatusNoAprobado_lanzaErrorEnStatus() {
		Termination termination = terminacionConStatus("PENDING_VALIDATION");

		assertThatThrownBy(() -> validation.validateAnnul(termination))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> org.assertj.core.api.Assertions.assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("status"));
	}

	@Test
	void validateAnnul_dentroDelPlazo_noLanzaExcepcion() {
		Termination termination = terminacionConStatus("APPROVED");
		when(businessDayCalculator.countBusinessDaysBetween(termination.getUpdatedAt().toLocalDate(), LocalDate.now()))
			.thenReturn(3L);

		assertThatCode(() -> validation.validateAnnul(termination)).doesNotThrowAnyException();
	}

	@Test
	void validateAnnul_fueraDelPlazo_lanzaErrorEnStatus() {
		Termination termination = terminacionConStatus("APPROVED");
		when(businessDayCalculator.countBusinessDaysBetween(termination.getUpdatedAt().toLocalDate(), LocalDate.now()))
			.thenReturn(6L);

		assertThatThrownBy(() -> validation.validateAnnul(termination))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> org.assertj.core.api.Assertions.assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("status"));
	}

	private Termination terminacionConStatus(String code) {
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);

		Termination termination = new Termination();
		termination.setStatus(status);
		termination.setUpdatedAt(LocalDateTime.now());
		return termination;
	}
}
