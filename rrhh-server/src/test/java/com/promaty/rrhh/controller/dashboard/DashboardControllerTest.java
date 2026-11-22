package com.promaty.rrhh.controller.dashboard;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.dashboard.BirthdayDto;
import com.promaty.rrhh.dto.dashboard.DashboardSummaryDto;
import com.promaty.rrhh.dto.dashboard.ProjectProgressDto;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.services.dashboard.DashboardService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class DashboardControllerTest {

	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DashboardService dashboardService;

	@Test
	void summary_autenticadoSinPermisos_retorna200ConIndicadoresEnNull() throws Exception {
		when(dashboardService.getSummary(null)).thenReturn(new DashboardSummaryDto());

		mockMvc.perform(get("/dashboard/summary").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.activeStaff").doesNotExist());
	}

	@Test
	void summary_conProjectId_loPasaAlService() throws Exception {
		DashboardSummaryDto resumen = new DashboardSummaryDto();
		resumen.setActiveStaff(42L);
		when(dashboardService.getSummary(7L)).thenReturn(resumen);

		mockMvc.perform(get("/dashboard/summary").param("projectId", "7").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.activeStaff").value(42));
		verify(dashboardService).getSummary(7L);
	}

	@Test
	void summary_conProyectoFueraDelAcceso_retorna403() throws Exception {
		when(dashboardService.getSummary(9L)).thenThrow(new AccessDeniedException("fuera de acceso"));

		mockMvc.perform(get("/dashboard/summary").param("projectId", "9").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void summary_sinToken_retorna401() throws Exception {
		mockMvc.perform(get("/dashboard/summary"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void birthdays_conStaffRead_retorna200ConLista() throws Exception {
		when(dashboardService.listBirthdays(null, null)).thenReturn(List.of(
			new BirthdayDto(1L, "Ana Soto Rojas", 3, 11, "00824")
		));

		mockMvc.perform(get("/dashboard/birthdays").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("staff.read")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].day").value(3))
			.andExpect(jsonPath("$.meta.pagination").doesNotExist());
	}

	@Test
	void birthdays_conMes_loPasaAlService() throws Exception {
		when(dashboardService.listBirthdays(240L, 10)).thenReturn(List.of(
			new BirthdayDto(7L, "Hernán Lorca Gaete", 28, 10, "00240")
		));

		mockMvc.perform(get("/dashboard/birthdays")
				.param("projectId", "240")
				.param("month", "10")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("staff.read")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].day").value(28))
			.andExpect(jsonPath("$.data[0].month").value(10));
	}

	@Test
	void birthdays_conMesFueraDeRango_retorna400ConErrorEnMonth() throws Exception {
		when(dashboardService.listBirthdays(null, 13))
			.thenThrow(new BusinessValidationException("El mes debe estar entre 1 y 12.", Map.of("month", "El mes debe estar entre 1 y 12.")));

		mockMvc.perform(get("/dashboard/birthdays")
				.param("month", "13")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("staff.read")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.month").value("El mes debe estar entre 1 y 12."));
	}

	@Test
	void birthdays_conMesNoNumerico_retorna400() throws Exception {
		mockMvc.perform(get("/dashboard/birthdays")
				.param("month", "octubre")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("staff.read")))
			.andExpect(status().isBadRequest());
	}

	@Test
	void birthdays_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/dashboard/birthdays").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}

	@Test
	void projectProgress_conProjectRead_retorna200ConLista() throws Exception {
		when(dashboardService.listProjectProgress(null)).thenReturn(List.of(
			new ProjectProgressDto(1L, "Edificio Central", "00824", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 87, 47L, 0L)
		));

		mockMvc.perform(get("/dashboard/projectProgress").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("project.read")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].elapsedPercent").value(87));
	}

	@Test
	void projectProgress_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/dashboard/projectProgress").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden());
	}
}
