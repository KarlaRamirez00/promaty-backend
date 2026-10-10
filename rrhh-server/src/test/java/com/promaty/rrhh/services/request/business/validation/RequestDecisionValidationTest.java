package com.promaty.rrhh.services.request.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.rrhh.dto.request.DecideRequestDto;
import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;

class RequestDecisionValidationTest {

	private final RequestRejectionReasonRepository requestRejectionReasonRepository = mock(RequestRejectionReasonRepository.class);
	private final RequestDecisionValidation requestDecisionValidation =
		new RequestDecisionValidation(requestRejectionReasonRepository);

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void validateDecision_enPendingApprovalConPermisoDeAprobar_retornaProjectManager() {
		autenticarCon("contract.approve");
		Request request = requestConStatus("PENDING_APPROVAL");

		ApprovalLevel nivel = requestDecisionValidation.validateDecision(request, decisionAprobada());

		assertThat(nivel).isEqualTo(ApprovalLevel.PROJECT_MANAGER);
	}

	@Test
	void validateDecision_enPendingValidationConPermisoDeValidar_retornaHR() {
		autenticarCon("contract.validate");
		Request request = requestConStatus("PENDING_VALIDATION");

		ApprovalLevel nivel = requestDecisionValidation.validateDecision(request, decisionAprobada());

		assertThat(nivel).isEqualTo(ApprovalLevel.HR);
	}

	@Test
	void validateDecision_enPendingApprovalSinPermisoDeAprobar_lanzaAccessDenied() {
		autenticarCon("contract.validate");
		Request request = requestConStatus("PENDING_APPROVAL");

		assertThatThrownBy(() -> requestDecisionValidation.validateDecision(request, decisionAprobada()))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void validateDecision_enStatusYaDecidido_lanzaBusinessValidation() {
		autenticarCon("contract.approve");
		Request request = requestConStatus("APPROVED");

		assertThatThrownBy(() -> requestDecisionValidation.validateDecision(request, decisionAprobada()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("status"));
	}

	@Test
	void validateDecision_rechazoSinMotivo_lanzaErrorEnRejectionReason() {
		autenticarCon("contract.approve");
		Request request = requestConStatus("PENDING_APPROVAL");

		DecideRequestDto dto = new DecideRequestDto();
		dto.setDecision(ApprovalDecision.REJECTED);

		assertThatThrownBy(() -> requestDecisionValidation.validateDecision(request, dto))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("rejectionReasonId"));
	}

	@Test
	void validateDecision_rechazoConMotivo_noLanzaExcepcion() {
		autenticarCon("contract.approve");
		Request request = requestConStatus("PENDING_APPROVAL");
		when(requestRejectionReasonRepository.existsById(1L)).thenReturn(true);

		DecideRequestDto dto = new DecideRequestDto();
		dto.setDecision(ApprovalDecision.REJECTED);
		dto.setRejectionReasonId(1L);

		assertThatCode(() -> requestDecisionValidation.validateDecision(request, dto))
			.doesNotThrowAnyException();
	}

	private void autenticarCon(String autoridad) {
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(autoridad));
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("usuario", null, authorities));
	}

	private Request requestConStatus(String code) {
		PlatformStatus status = new PlatformStatus();
		status.setCode(code);
		Request request = new Request();
		request.setStatus(status);
		return request;
	}

	private DecideRequestDto decisionAprobada() {
		DecideRequestDto dto = new DecideRequestDto();
		dto.setDecision(ApprovalDecision.APPROVED);
		return dto;
	}
}
