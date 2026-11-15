package com.promaty.rrhh.services.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.promaty.rrhh.dto.request.TerminationPendingDataDto;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.RequestRepository;

@ExtendWith(MockitoExtension.class)
class RequestEditPolicyTest {

	@Mock
	private RequestRepository requestRepository;
	@Mock
	private PlatformStatusRepository platformStatusRepository;
	@Spy
	private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@InjectMocks
	private RequestEditPolicy policy;

	@ParameterizedTest
	@ValueSource(strings = {"PENDING_APPROVAL", "PENDING_VALIDATION", "REJECTED"})
	void isEditable_enEstadosAbiertos_esVerdadero(String code) {
		assertThat(policy.isEditable(requestEn(code))).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"APPROVED", "ANNULLED"})
	void isEditable_aprobadoOAnulado_esFalso(String code) {
		assertThat(policy.isEditable(requestEn(code))).isFalse();
	}

	@Test
	void isEditable_sinSolicitud_esFalso() {
		assertThat(policy.isEditable(null)).isFalse();
	}

	@Test
	void requireEditable_aprobado_lanzaErrorEnStatus() {
		when(requestRepository.findByEntityTypeAndEntityId(RequestEntityType.CONTRACT, 1L)).thenReturn(Optional.of(requestEn("APPROVED")));

		assertThatThrownBy(() -> policy.requireEditable(RequestEntityType.CONTRACT, 1L))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields()).containsKey("status"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"PENDING_VALIDATION", "REJECTED"})
	void returnToPendingApproval_trasAprobacionORechazo_vuelveAlPrimerNivel(String code) {
		Request request = requestEn(code);
		PlatformStatus pendiente = estado("PENDING_APPROVAL");
		when(platformStatusRepository.findBySubModuleAndCode("request", "PENDING_APPROVAL")).thenReturn(Optional.of(pendiente));

		assertThat(policy.returnToPendingApproval(request)).isTrue();

		assertThat(request.getStatus()).isEqualTo(pendiente);
		verify(requestRepository).save(request);
	}

	@Test
	void returnToPendingApproval_yaEnPendingApproval_noCambiaNada() {
		Request request = requestEn("PENDING_APPROVAL");

		assertThat(policy.returnToPendingApproval(request)).isFalse();

		verify(requestRepository, never()).save(any());
	}

	@Test
	void replacePendingData_guardaLosDatosEditadosComoJson() {
		Request request = requestEn("REJECTED");
		TerminationPendingDataDto datos = new TerminationPendingDataDto();
		datos.setStaffId(2L);
		datos.setTerminationDate(LocalDate.of(2026, 12, 1));

		policy.replacePendingData(request, datos);

		assertThat(request.getPendingData()).contains("\"staffId\":2");
		verify(requestRepository).save(request);
	}

	private static Request requestEn(String code) {
		Request request = new Request();
		request.setStatus(estado(code));
		return request;
	}

	private static PlatformStatus estado(String code) {
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);
		return status;
	}
}
