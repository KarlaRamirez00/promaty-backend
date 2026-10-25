package com.promaty.rrhh.controller.termination;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.termination.TerminationDetailDto;
import com.promaty.rrhh.dto.termination.TerminationListDto;
import com.promaty.rrhh.dto.termination.StaffSummaryDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.termination.TerminationService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(TerminationController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class TerminationControllerTest {

	private static final String TOKEN = TestJwt.bearer("termination.read", "termination.update", "termination.annul");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	private static final String ANNUL_JSON = """
		{
		  "reason": "Error administrativo al aprobar."
		}
		""";

	private static final String UPDATE_JSON = """
		{
		  "terminationDate": "2026-01-15",
		  "legalTerminationCauseId": 10,
		  "definitiveTermination": false
		}
		""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TerminationService terminationService;

	@Test
	void list_retorna200ConData() throws Exception {
		StaffSummaryDto staff = new StaffSummaryDto(1L, "Juan Perez Soto", "12345678-5");
		Page<TerminationListDto> pagina = new PageImpl<>(List.of(
			new TerminationListDto(1L, staff, "00824", "Renuncia voluntaria", LocalDate.of(2026, 1, 15),
				false, "PENDING_APPROVAL", "Pendiente de aprobación", null, null, "system", null, null, List.of())
		));
		when(terminationService.listTerminations(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/terminations").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].costCenterCode").value("00824"));
	}

	@Test
	void detail_registroExiste_retorna200() throws Exception {
		StaffSummaryDto staff = new StaffSummaryDto(1L, "Juan Perez Soto", "12345678-5");
		TerminationDetailDto detalle = new TerminationDetailDto(1L, staff, "00824", "Albañil",
			LocalDate.of(2025, 1, 1), null, LocalDate.of(2026, 1, 15), null, false, null, null,
			null, null, null, null, null, "system", null, null, List.of());
		when(terminationService.getTerminationDetail(1L)).thenReturn(detalle);

		mockMvc.perform(get("/terminations/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.staff.identificationNumber").value("12345678-5"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(terminationService.getTerminationDetail(99L))
			.thenThrow(new ResourceNotFoundException("Finiquito no encontrado."));

		mockMvc.perform(get("/terminations/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound());
	}

	@Test
	void update_conDatosValidos_retorna200() throws Exception {
		StaffSummaryDto staff = new StaffSummaryDto(1L, "Juan Perez Soto", "12345678-5");
		TerminationDetailDto detalle = new TerminationDetailDto(1L, staff, "00824", "Albañil",
			LocalDate.of(2025, 1, 1), null, LocalDate.of(2026, 1, 15), null, false, null, null,
			null, null, null, null, null, "system", null, null, List.of());
		when(terminationService.getTerminationDetail(1L)).thenReturn(detalle);

		mockMvc.perform(put("/terminations/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(UPDATE_JSON))
			.andExpect(status().isOk());
	}

	@Test
	void annul_conMotivoValido_retorna200() throws Exception {
		StaffSummaryDto staff = new StaffSummaryDto(1L, "Juan Perez Soto", "12345678-5");
		TerminationDetailDto detalle = new TerminationDetailDto(1L, staff, "00824", "Albañil",
			LocalDate.of(2025, 1, 1), null, LocalDate.of(2026, 1, 15), null, false, null, null,
			null, null, null, null, null, "system", null, null, List.of());
		when(terminationService.getTerminationDetail(1L)).thenReturn(detalle);

		mockMvc.perform(patch("/terminations/1/annul")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ANNUL_JSON))
			.andExpect(status().isOk());
	}

	@Test
	void annul_conMotivoEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(patch("/terminations/1/annul")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"reason\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.reason").exists());
	}

	@Test
	void annul_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(patch("/terminations/1/annul")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ANNUL_JSON))
			.andExpect(status().isForbidden());
	}

	@Test
	void list_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/terminations").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void update_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(put("/terminations/1")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(UPDATE_JSON))
			.andExpect(status().isForbidden());
	}
}
