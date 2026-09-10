package com.promaty.rrhh.controller.platformstatus;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.services.platformstatus.PlatformStatusService;
import com.promaty.rrhh.support.TestJwt;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(PlatformStatusController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class PlatformStatusControllerTest {

	// Token con el permiso de platformStatus para los tests de flujo; la autorizacion por permiso se prueba aparte.
	private static final String TOKEN = TestJwt.bearer("platformStatus.read");

	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PlatformStatusService platformStatusService;

	@Test
	void list_conSubModule_retorna200ConOpcionesYSinPaginacion() throws Exception {
		when(platformStatusService.listOptionsBySubModule(eq("project"))).thenReturn(List.of(
			new PlatformStatusOptionDto(1L, "PLANNED", "Planificado"),
			new PlatformStatusOptionDto(2L, "IN_PROGRESS", "En ejecución")
		));

		mockMvc.perform(get("/platformStatuses").param("subModule", "project").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(2))
			.andExpect(jsonPath("$.data[0].code").value("PLANNED"))
			.andExpect(jsonPath("$.data[1].name").value("En ejecución"))
			.andExpect(jsonPath("$.error").doesNotExist())
			.andExpect(jsonPath("$.meta.pagination").doesNotExist());
	}

	@Test
	void list_sinSubModule_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(get("/platformStatuses").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.subModule").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	// --- Autorizacion por permiso (@PreAuthorize) ---
	// El happy-path con el permiso exacto ya lo cubre list_conSubModule_retorna200...; aca solo falta
	// el rechazo sin el permiso.
	@Test
	void list_sinPermiso_retorna403ConShapeDeContrato() throws Exception {
		mockMvc.perform(get("/platformStatuses").param("subModule", "project").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403))
			.andExpect(jsonPath("$.error.name").value("FORBIDDEN"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}
}
