package com.promaty.rrhh.controller.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.contract.ContractCountersDto;
import com.promaty.rrhh.dto.contract.ContractDetailDto;
import com.promaty.rrhh.dto.contract.ContractFilterParams;
import com.promaty.rrhh.dto.contract.ContractListDto;
import com.promaty.rrhh.dto.contract.StaffSummaryDto;
import com.promaty.rrhh.dto.contract.UpdateContractAttachmentsDto;
import com.promaty.rrhh.dto.contract.UpdateContractDto;
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
	private static final String EDICION = TestJwt.bearer("contract.update");
	private static final String UPDATE_JSON = """
		{ "projectId": 2, "companyId": 3, "contractTypeId": 4, "jobTitleId": 5, "siteId": 6,
		  "startDate": "2026-12-01", "baseSalary": 900000, "weeklyWorkHours": 45, "workDays": 5 }
		""";
	private static final String ATTACHMENTS_JSON = """
		{ "attachments": [ { "fileIndex": 0, "type": "CERT_AFP" } ] }
		""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ContractService contractService;

	@Test
	void list_retorna200ConData() throws Exception {
		StaffSummaryDto staff = new StaffSummaryDto(1L, "Juan Perez Soto", "12345678-5");
		Page<ContractListDto> pagina = new PageImpl<>(List.of(
			new ContractListDto(1L, staff, "00824", "Contrato indefinido", "Albañil", "PENDING_APPROVAL",
				"Pendiente de aprobación", LocalDate.of(2026, 1, 1), null, null, null, null, "system", null, null, List.of())
		));
		when(contractService.listContracts(any(), any())).thenReturn(pagina);
		when(contractService.getCounters()).thenReturn(new ContractCountersDto(3, 1, 7));

		mockMvc.perform(get("/contracts").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].costCenterCode").value("00824"))
			.andExpect(jsonPath("$.meta.otherData.expired").value(3))
			.andExpect(jsonPath("$.meta.otherData.expiringSoon").value(1))
			.andExpect(jsonPath("$.meta.otherData.expiringSoonDays").value(7));
	}

	@Test
	void list_conExpiringSoon_pasaElFiltroAlService() throws Exception {
		when(contractService.listContracts(any(), any())).thenReturn(Page.empty());
		when(contractService.getCounters()).thenReturn(new ContractCountersDto(0, 0, 7));

		mockMvc.perform(get("/contracts").param("expiringSoon", "true").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk());

		ArgumentCaptor<ContractFilterParams> captor = ArgumentCaptor.forClass(ContractFilterParams.class);
		verify(contractService).listContracts(captor.capture(), any());
		assertThat(captor.getValue().getExpiringSoon()).isTrue();
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
			LocalDate.of(2026, 1, 1), null, null, null, null, null, null, null, null, null,
			null, null, null, "system", null, null, List.of(), null, null);
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

	@Test
	void update_conPermiso_retorna200YDelegaEnElService() throws Exception {
		when(contractService.getContractDetail(1L)).thenReturn(new ContractDetailDto());

		mockMvc.perform(contractPut("/contracts/1", EDICION, UPDATE_JSON))
			.andExpect(status().isOk());

		verify(contractService).updateContract(eq(1L), any(UpdateContractDto.class), any());
	}

	@Test
	void update_sinCentroDeCosto_retorna400ConElCampo() throws Exception {
		mockMvc.perform(contractPut("/contracts/1", EDICION, UPDATE_JSON.replace("\"projectId\": 2,", "")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.projectId").exists());
	}

	@Test
	void update_soloConLectura_retorna403() throws Exception {
		mockMvc.perform(contractPut("/contracts/1", TOKEN, UPDATE_JSON))
			.andExpect(status().isForbidden());
	}

	@Test
	void updateAttachments_conPermiso_retorna200YDelegaConLosArchivos() throws Exception {
		when(contractService.getContractDetail(1L)).thenReturn(new ContractDetailDto());

		mockMvc.perform(attachmentsPut(EDICION, ATTACHMENTS_JSON).file(pdf()))
			.andExpect(status().isOk());

		verify(contractService).updateAttachments(eq(1L), any(UpdateContractAttachmentsDto.class), argThat(files -> files.size() == 1));
	}

	@Test
	void updateAttachments_sinListaDeAdjuntos_retorna400() throws Exception {
		mockMvc.perform(attachmentsPut(EDICION, "{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.attachments").exists());
	}

	@Test
	void updateAttachments_soloConLectura_retorna403() throws Exception {
		mockMvc.perform(attachmentsPut(TOKEN, ATTACHMENTS_JSON))
			.andExpect(status().isForbidden());
	}

	private static MockMultipartHttpServletRequestBuilder attachmentsPut(String token, String json) {
		return contractPut("/contracts/1/attachments", token, json);
	}

	private static MockMultipartHttpServletRequestBuilder contractPut(String url, String token, String json) {
		MockMultipartHttpServletRequestBuilder builder = multipart(HttpMethod.PUT, url);
		builder.file(new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes(StandardCharsets.UTF_8)));
		builder.header(HttpHeaders.AUTHORIZATION, token);
		return builder;
	}

	private static MockMultipartFile pdf() {
		return new MockMultipartFile("files", "afp.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[] {0x25, 0x50, 0x44, 0x46});
	}
}
