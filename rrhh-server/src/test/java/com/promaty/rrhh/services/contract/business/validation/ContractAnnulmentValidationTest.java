package com.promaty.rrhh.services.contract.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.holidayprovider.BusinessDayCalculator;

@ExtendWith(MockitoExtension.class)
class ContractAnnulmentValidationTest {

	@Mock
	private BusinessDayCalculator businessDayCalculator;
	@Mock
	private TerminationRepository terminationRepository;
	@Mock
	private AnnexRepository annexRepository;

	@InjectMocks
	private ContractAnnulmentValidation validation;

	@Test
	void validateAnnul_contratoNoVigente_lanzaErrorEnStatusSinConsultarNada() {
		Contract contract = contratoConStatus("PENDING_VALIDATION");

		assertThatThrownBy(() -> validation.validateAnnul(contract))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields()).containsKey("status"));

		verifyNoInteractions(businessDayCalculator, terminationRepository, annexRepository);
	}

	@Test
	void validateAnnul_vigenteDentroDelPlazoSinFiniquitosNiAnexos_noLanzaExcepcion() {
		Contract contract = contratoConStatus("ACTIVE");
		diasHabilesTranscurridos(contract, 5L);

		assertThatCode(() -> validation.validateAnnul(contract)).doesNotThrowAnyException();
	}

	@Test
	void validateAnnul_fueraDelPlazo_lanzaErrorEnStatus() {
		Contract contract = contratoConStatus("ACTIVE");
		diasHabilesTranscurridos(contract, 6L);

		assertThatThrownBy(() -> validation.validateAnnul(contract))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields()).containsKey("status"));
	}

	@Test
	void validateAnnul_conFiniquitoEnCurso_lanzaErrorEnTerminations() {
		Contract contract = contratoConStatus("ACTIVE");
		diasHabilesTranscurridos(contract, 1L);
		when(terminationRepository.existsByContract_IdAndStatus_CodeIn(eq(1L), anyList())).thenReturn(true);

		assertThatThrownBy(() -> validation.validateAnnul(contract))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("terminations").doesNotContainKey("annexes"));
	}

	@Test
	void validateAnnul_conAnexoEnCursoYFueraDePlazo_acumulaAmbosErrores() {
		Contract contract = contratoConStatus("ACTIVE");
		diasHabilesTranscurridos(contract, 9L);
		when(annexRepository.existsByContract_IdAndStatus_CodeIn(eq(1L), anyList())).thenReturn(true);

		assertThatThrownBy(() -> validation.validateAnnul(contract))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKeys("status", "annexes"));
	}

	private void diasHabilesTranscurridos(Contract contract, long dias) {
		when(businessDayCalculator.countBusinessDaysBetween(contract.getUpdatedAt().toLocalDate(), LocalDate.now()))
			.thenReturn(dias);
	}

	private Contract contratoConStatus(String code) {
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);

		Contract contract = new Contract();
		contract.setId(1L);
		contract.setStatus(status);
		contract.setUpdatedAt(LocalDateTime.now());
		return contract;
	}
}
