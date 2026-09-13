package com.promaty.rrhh.controller.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.dto.project.ProjectDetailDto;
import com.promaty.rrhh.dto.project.ProjectListDto;
import com.promaty.rrhh.dto.project.RelationSummaryDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.project.ProjectService;
import com.promaty.rrhh.support.TestJwt;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(ProjectController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class ProjectControllerTest {

	private static final String PROYECTO_JSON =
		"{\"name\":\"Edificio Norte\",\"costCenterCode\":\"00824\",\"typeId\":10,\"specialtyId\":20,"
			+ "\"clientId\":30,\"startDate\":\"2026-01-01\"}";

	// Token con todos los permisos de project para los tests de flujo; la autorizacion por permiso se prueba aparte.
	private static final String TOKEN =
		TestJwt.bearer("project.read", "project.create", "project.update", "project.status");

	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProjectService projectService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(projectService.createProject(any())).thenReturn(10L);

		mockMvc.perform(post("/projects")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(PROYECTO_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_sinCamposObligatorios_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/projects")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.name").exists())
			.andExpect(jsonPath("$.error.errorFields.typeId").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void list_retorna200ConDataYPaginacion() throws Exception {
		Page<ProjectListDto> pagina = new PageImpl<>(
			List.of(new ProjectListDto(1L, "Edificio Norte", "00824", "Obra gruesa", "Eléctrica", "Sodimac", "En ejecución",
				LocalDate.of(2026, 1, 1), null, "system", null, List.of())),
			PageRequest.of(0, 20),
			1
		);
		when(projectService.listProjects(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/projects").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Edificio Norte"))
			.andExpect(jsonPath("$.data[0].statusName").value("En ejecución"))
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_registroExiste_retorna200ConData() throws Exception {
		when(projectService.getProjectDetail(1L)).thenReturn(detalleDto());

		mockMvc.perform(get("/projects/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Edificio Norte"))
			.andExpect(jsonPath("$.data.type.name").value("Obra gruesa"))
			.andExpect(jsonPath("$.data.status.code").value("IN_PROGRESS"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(projectService.getProjectDetail(99L))
			.thenThrow(new ResourceNotFoundException("Proyecto no encontrado."));

		mockMvc.perform(get("/projects/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Proyecto no encontrado."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(projectService.getProjectDetail(1L)).thenReturn(detalleDto());

		mockMvc.perform(put("/projects/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(PROYECTO_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Edificio Norte"));
	}

	@Test
	void updateStatus_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(projectService.updateProjectStatus(any(), any())).thenReturn(detalleDto());

		mockMvc.perform(patch("/projects/1/status")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"statusId\":40}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.status.code").value("IN_PROGRESS"));
	}

	@Test
	void updateStatus_sinStatusId_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(patch("/projects/1/status")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.statusId").exists());
	}

	// --- Autorizacion por permiso (@PreAuthorize) ---
	// Cada endpoint exige su authority exacta: sin permiso -> 403 con el shape del contrato; con el
	// permiso exacto -> 2xx. create_conPermisoDeOtraAccion prueba que no basta con tener "algun"
	// permiso de project.

	@Test
	void list_sinPermiso_retorna403ConShapeDeContrato() throws Exception {
		mockMvc.perform(get("/projects").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403))
			.andExpect(jsonPath("$.error.name").value("FORBIDDEN"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void detail_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/projects/1").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/projects")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(PROYECTO_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void update_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(put("/projects/1")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(PROYECTO_JSON))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void updateStatus_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(patch("/projects/1/status")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"statusId\":40}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_conPermisoDeOtraAccion_retorna403() throws Exception {
		mockMvc.perform(post("/projects")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("project.read"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(PROYECTO_JSON))
			.andExpect(status().isForbidden());
	}

	@Test
	void list_conPermisoExacto_retorna200() throws Exception {
		when(projectService.listProjects(any(), any())).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/projects").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("project.read")))
			.andExpect(status().isOk());
	}

	@Test
	void detail_conPermisoExacto_retorna200() throws Exception {
		when(projectService.getProjectDetail(1L)).thenReturn(detalleDto());

		mockMvc.perform(get("/projects/1").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("project.read")))
			.andExpect(status().isOk());
	}

	@Test
	void create_conPermisoExacto_retorna201() throws Exception {
		when(projectService.createProject(any())).thenReturn(10L);

		mockMvc.perform(post("/projects")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("project.create"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(PROYECTO_JSON))
			.andExpect(status().isCreated());
	}

	@Test
	void update_conPermisoExacto_retorna200() throws Exception {
		when(projectService.getProjectDetail(1L)).thenReturn(detalleDto());

		mockMvc.perform(put("/projects/1")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("project.update"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(PROYECTO_JSON))
			.andExpect(status().isOk());
	}

	@Test
	void updateStatus_conPermisoExacto_retorna200() throws Exception {
		when(projectService.updateProjectStatus(any(), any())).thenReturn(detalleDto());

		mockMvc.perform(patch("/projects/1/status")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("project.status"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"statusId\":40}"))
			.andExpect(status().isOk());
	}

	private ProjectDetailDto detalleDto() {
		return new ProjectDetailDto(
			1L,
			"Edificio Norte",
			"00824",
			new RelationSummaryDto(10L, "Obra gruesa"),
			new RelationSummaryDto(20L, "Eléctrica"),
			new RelationSummaryDto(30L, "Sodimac"),
			new PlatformStatusOptionDto(40L, "IN_PROGRESS", "En ejecución"),
			LocalDate.of(2026, 1, 1),
			null,
			null,
			null,
			"system",
			null,
			List.of()
		);
	}
}
