package com.promaty.rrhh.services.request.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.promaty.rrhh.dto.request.ContractPendingDataDto;
import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.security.JwtPrincipal;

@ExtendWith(MockitoExtension.class)
class CreateRequestBuilderTest {

	@Mock
	private RequestRelationsResolver relationsResolver;

	private CreateRequestBuilder createRequestBuilder;

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void build_conDatosValidos_armaRequestConPendingDataSerializado() {
		createRequestBuilder = new CreateRequestBuilder(relationsResolver, new ObjectMapper().registerModule(new JavaTimeModule()));

		Project project = new Project();
		project.setId(1L);
		PlatformStatus statusInicial = new PlatformStatus();
		statusInicial.setId(1L);
		statusInicial.setCode("PENDING_APPROVAL");
		when(relationsResolver.resolveProject(1L)).thenReturn(project);
		when(relationsResolver.resolveInitialStatus()).thenReturn(statusInicial);
		autenticarComo("7");

		Request request = createRequestBuilder.build(dtoValido());

		assertThat(request.getEntityType()).isEqualTo(RequestEntityType.CONTRACT);
		assertThat(request.getAction()).isEqualTo(RequestAction.CREATE);
		assertThat(request.getProject()).isEqualTo(project);
		assertThat(request.getStatus()).isEqualTo(statusInicial);
		assertThat(request.getRequesterUserId()).isEqualTo(7L);
		assertThat(request.getPendingData()).contains("\"staffId\":2");
	}

	private void autenticarComo(String userId) {
		JwtPrincipal principal = new JwtPrincipal(userId, "ROLE", "Jefe de obra", List.of(), false, List.of());
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(principal, null, List.of()));
	}

	private CreateRequestDto dtoValido() {
		ContractPendingDataDto contractData = new ContractPendingDataDto();
		contractData.setStaffId(2L);
		contractData.setCompanyId(3L);
		contractData.setContractTypeId(4L);
		contractData.setJobTitleId(5L);
		contractData.setSiteId(6L);
		contractData.setStartDate(LocalDate.of(2026, 1, 1));
		contractData.setBaseSalary(BigDecimal.valueOf(850000));
		contractData.setWeeklyWorkHours(45);
		contractData.setWorkDays(5);

		CreateRequestDto dto = new CreateRequestDto();
		dto.setEntityType(RequestEntityType.CONTRACT);
		dto.setAction(RequestAction.CREATE);
		dto.setProjectId(1L);
		dto.setContractData(contractData);
		return dto;
	}
}
