package com.promaty.rrhh.controller.projectspecialty;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyDetailDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyListDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.projectspecialty.ProjectSpecialtyService;
import com.promaty.rrhh.support.TestJwt;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(ProjectSpecialtyController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class ProjectSpecialtyControllerTest {

	// Token con todos los permisos de projectSpecialty para los tests de flujo; la autorizacion por permiso se prueba aparte.
	private static final String TOKEN = TestJwt.bearer(
		"projectSpecialty.read", "projectSpecialty.create", "projectSpecialty.update", "projectSpecialty.active");

	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProjectSpecialtyService projectSpecialtyService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(projectSpecialtyService.createProjectSpecialty(any())).thenReturn(10L);

		mockMvc.perform(post("/projectSpecialties")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Eléctrica\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_conNombreEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/projectSpecialties")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.name").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void list_retorna200ConDataYPaginacion() throws Exception {
		Page<ProjectSpecialtyListDto> pagina = new PageImpl<>(
			List.of(new ProjectSpecialtyListDto(1L, "Eléctrica", true, LocalDateTime.of(2026, 1, 15, 10, 0), null, List.of())),
			PageRequest.of(0, 20),
			1
		);
		when(projectSpecialtyService.listProjectSpecialties(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/projectSpecialties").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Eléctrica"))
			.andExpect(jsonPath("$.data[0].createdAt").exists())
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_registroExiste_retorna200ConData() throws Exception {
		when(projectSpecialtyService.getProjectSpecialtyDetail(1L))
			.thenReturn(new ProjectSpecialtyDetailDto(1L, "Eléctrica", true, null, null, List.of()));

		mockMvc.perform(get("/projectSpecialties/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Eléctrica"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(projectSpecialtyService.getProjectSpecialtyDetail(99L))
			.thenThrow(new ResourceNotFoundException("Especialidad no encontrada."));

		mockMvc.perform(get("/projectSpecialties/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Especialidad no encontrada."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(projectSpecialtyService.getProjectSpecialtyDetail(1L))
			.thenReturn(new ProjectSpecialtyDetailDto(1L, "Sanitaria", true, null, null, List.of()));

		mockMvc.perform(put("/projectSpecialties/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Sanitaria\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Sanitaria"));
	}

	@Test
	void toggleActive_retorna200ConFlagAlternado() throws Exception {
		when(projectSpecialtyService.toggleProjectSpecialtyActive(1L))
			.thenReturn(new ProjectSpecialtyDetailDto(1L, "Eléctrica", false, null, null, List.of()));

		mockMvc.perform(patch("/projectSpecialties/1/active").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(false));
	}

	// --- Autorizacion por permiso (@PreAuthorize) ---
	// Cada endpoint exige su authority exacta: sin permiso -> 403 con el shape del contrato; con el
	// permiso exacto -> 2xx. create_conPermisoDeOtraAccion prueba que no basta con tener "algun"
	// permiso de projectSpecialty.

	@Test
	void list_sinPermiso_retorna403ConShapeDeContrato() throws Exception {
		mockMvc.perform(get("/projectSpecialties").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403))
			.andExpect(jsonPath("$.error.name").value("FORBIDDEN"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void detail_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/projectSpecialties/1").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/projectSpecialties")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Eléctrica\"}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void update_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(put("/projectSpecialties/1")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Sanitaria\"}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void toggleActive_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(patch("/projectSpecialties/1/active").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_conPermisoDeOtraAccion_retorna403() throws Exception {
		mockMvc.perform(post("/projectSpecialties")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectSpecialty.read"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Eléctrica\"}"))
			.andExpect(status().isForbidden());
	}

	@Test
	void list_conPermisoExacto_retorna200() throws Exception {
		when(projectSpecialtyService.listProjectSpecialties(any(), any())).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/projectSpecialties").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectSpecialty.read")))
			.andExpect(status().isOk());
	}

	@Test
	void detail_conPermisoExacto_retorna200() throws Exception {
		when(projectSpecialtyService.getProjectSpecialtyDetail(1L))
			.thenReturn(new ProjectSpecialtyDetailDto(1L, "Eléctrica", true, null, null, List.of()));

		mockMvc.perform(get("/projectSpecialties/1").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectSpecialty.read")))
			.andExpect(status().isOk());
	}

	@Test
	void create_conPermisoExacto_retorna201() throws Exception {
		when(projectSpecialtyService.createProjectSpecialty(any())).thenReturn(10L);

		mockMvc.perform(post("/projectSpecialties")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectSpecialty.create"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Eléctrica\"}"))
			.andExpect(status().isCreated());
	}

	@Test
	void update_conPermisoExacto_retorna200() throws Exception {
		when(projectSpecialtyService.getProjectSpecialtyDetail(1L))
			.thenReturn(new ProjectSpecialtyDetailDto(1L, "Sanitaria", true, null, null, List.of()));

		mockMvc.perform(put("/projectSpecialties/1")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectSpecialty.update"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Sanitaria\"}"))
			.andExpect(status().isOk());
	}

	@Test
	void toggleActive_conPermisoExacto_retorna200() throws Exception {
		when(projectSpecialtyService.toggleProjectSpecialtyActive(1L))
			.thenReturn(new ProjectSpecialtyDetailDto(1L, "Eléctrica", false, null, null, List.of()));

		mockMvc.perform(patch("/projectSpecialties/1/active")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectSpecialty.active")))
			.andExpect(status().isOk());
	}
}
