package com.promaty.rrhh.services.termination.business.validation;

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

import com.promaty.rrhh.dto.termination.UpdateTerminationDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;

@ExtendWith(MockitoExtension.class)
class TerminationValidationTest {

	@Mock
	private LegalTerminationCauseRepository legalTerminationCauseRepository;
	@Mock
	private NonRehireReasonRepository nonRehireReasonRepository;

	@InjectMocks
	private TerminationValidation terminationValidation;

	@Test
	void validateUpdate_conDatosValidos_noLanzaExcepcion() {
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));

		assertThatCode(() -> terminationValidation.validateUpdate(terminacionEditable(), dtoValido()))
			.doesNotThrowAnyException();
	}

	@Test
	void validateUpdate_statusYaDecidido_lanzaErrorEnStatus() {
		Termination existente = terminacionConEstado("APPROVED");
		existente.setContract(contratoActivo());

		assertThatThrownBy(() -> terminationValidation.validateUpdate(existente, dtoValido()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("status"));
	}

	@Test
	void validateUpdate_causaLegalInexistente_lanzaErrorEnLegalTerminationCauseId() {
		when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> terminationValidation.validateUpdate(terminacionEditable(), dtoValido()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("legalTerminationCauseId"));
	}

	@Test
	void validateUpdate_definitivoSinMotivo_lanzaErrorEnNonRehireReasonId() {
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));

		UpdateTerminationDto dto = dtoValido();
		dto.setDefinitiveTermination(true);
		dto.setNonRehireReasonId(null);

		assertThatThrownBy(() -> terminationValidation.validateUpdate(terminacionEditable(), dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("nonRehireReasonId"));
	}

	@Test
	void validateUpdate_noDefinitivoConMotivo_lanzaErrorEnNonRehireReasonId() {
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));

		UpdateTerminationDto dto = dtoValido();
		dto.setDefinitiveTermination(false);
		dto.setNonRehireReasonId(20L);

		assertThatThrownBy(() -> terminationValidation.validateUpdate(terminacionEditable(), dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("nonRehireReasonId"));
	}

	@Test
	void validateUpdate_fechaMuyRetroactiva_lanzaErrorEnTerminationDate() {
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));

		UpdateTerminationDto dto = dtoValido();
		dto.setTerminationDate(LocalDate.now().minusDays(30));

		assertThatThrownBy(() -> terminationValidation.validateUpdate(terminacionEditable(), dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("terminationDate"));
	}

	@Test
	void validateUpdate_motivoDeNoRecontratacionInactivo_lanzaErrorEnNonRehireReasonId() {
		lenient().when(legalTerminationCauseRepository.findById(10L)).thenReturn(Optional.of(causaLegalActiva()));
		NonRehireReason motivoInactivo = new NonRehireReason();
		motivoInactivo.setActive(false);
		when(nonRehireReasonRepository.findById(20L)).thenReturn(Optional.of(motivoInactivo));

		UpdateTerminationDto dto = dtoValido();
		dto.setDefinitiveTermination(true);
		dto.setNonRehireReasonId(20L);

		assertThatThrownBy(() -> terminationValidation.validateUpdate(terminacionEditable(), dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("nonRehireReasonId"));
	}

	private Termination terminacionEditable() {
		return terminacionConEstado("PENDING_APPROVAL");
	}

	private Termination terminacionConEstado(String code) {
		Termination termination = new Termination();
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);
		termination.setStatus(status);
		termination.setContract(contratoActivo());
		return termination;
	}

	private Contract contratoActivo() {
		Contract contrato = new Contract();
		contrato.setStartDate(LocalDate.now().minusYears(1));
		return contrato;
	}

	private LegalTerminationCause causaLegalActiva() {
		LegalTerminationCause causa = new LegalTerminationCause();
		causa.setActive(true);
		return causa;
	}

	private UpdateTerminationDto dtoValido() {
		UpdateTerminationDto dto = new UpdateTerminationDto();
		dto.setTerminationDate(LocalDate.now());
		dto.setLegalTerminationCauseId(10L);
		dto.setDefinitiveTermination(false);
		return dto;
	}
}
