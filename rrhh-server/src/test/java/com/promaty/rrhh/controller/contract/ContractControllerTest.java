package com.promaty.rrhh.controller.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.contract.ContractCountersDto;
import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.dto.contract.StaffSummaryDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.contract.ContractService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(ContractController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class ContractControllerTest {

	private static final String TOKEN = TestJwt.bearer("contract.read");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ContractService contractService;

	@Test
	void list_retorna200ConData() throws Exception {
		StaffSummaryDto staff = new StaffSummaryDto(1L, "Juan Perez Soto", "12345678-5");
		Page<ContractListDto> pagina = new PageImpl<>(List.of(
			new ContractListDto(1L, staff, null, null, "00824", "Contrato indefinido", "Albañil", "PENDING_APPROVAL",
				"Pendiente de aprobación", LocalDate.of(2026, 1, 1), null, null, null, null, "system", null, List.of())
		));
		when(contractService.listContracts(any(), any())).thenReturn(pagina);
		when(contractService.getCounters()).thenReturn(new ContractCountersDto(3, 1));

		mockMvc.perform(get("/contracts").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].costCenterCode").value("00824"))
			.andExpect(jsonPath("$.meta.otherData.expired").value(3))
			.andExpect(jsonPath("$.meta.otherData.expiringSoon").value(1));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(contractService.getContractDetail(99L))
			.thenThrow(new ResourceNotFoundException("Contrato no encontrado."));

		mockMvc.perform(get("/contracts/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound());
	}

	@Test
	void detail_registroExiste_retorna200() throws Exception {
		StaffSummaryDto staff = new StaffSummaryDto(1L, "Juan Perez Soto", "12345678-5");
		ContractDetailDto detalle = new ContractDetailDto(1L, staff, null, null, null, null, "00824",
			LocalDate.of(2026, 1, 1), null, null, null, null, null, null, null, null, null, null,
			null, null, null, "system", null, List.of());
		when(contractService.getContractDetail(1L)).thenReturn(detalle);

		mockMvc.perform(get("/contracts/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.staff.identificationNumber").value("12345678-5"));
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/contracts").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}
}
