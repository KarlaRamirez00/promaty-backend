package com.promaty.rrhh.controller.projecttype;

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
import com.promaty.rrhh.dto.projecttype.ProjectTypeDetailDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeListDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.projecttype.ProjectTypeService;
import com.promaty.rrhh.support.TestJwt;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(ProjectTypeController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class ProjectTypeControllerTest {

	// Token con todos los permisos de projectType para los tests de flujo; la autorizacion por permiso se prueba aparte.
	private static final String TOKEN =
		TestJwt.bearer("projectType.read", "projectType.create", "projectType.update", "projectType.active");

	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProjectTypeService projectTypeService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(projectTypeService.createProjectType(any())).thenReturn(10L);

		mockMvc.perform(post("/projectTypes")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Obra gruesa\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_conNombreEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/projectTypes")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.name").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void list_retorna200ConDataYPaginacion() throws Exception {
		Page<ProjectTypeListDto> pagina = new PageImpl<>(
			List.of(new ProjectTypeListDto(1L, "Obra gruesa", true, LocalDateTime.of(2026, 1, 15, 10, 0), null)),
			PageRequest.of(0, 20),
			1
		);
		when(projectTypeService.listProjectTypes(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/projectTypes").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Obra gruesa"))
			.andExpect(jsonPath("$.data[0].createdAt").exists())
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_registroExiste_retorna200ConData() throws Exception {
		when(projectTypeService.getProjectTypeDetail(1L))
			.thenReturn(new ProjectTypeDetailDto(1L, "Obra gruesa", true, null, null));

		mockMvc.perform(get("/projectTypes/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Obra gruesa"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(projectTypeService.getProjectTypeDetail(99L))
			.thenThrow(new ResourceNotFoundException("Tipo de proyecto no encontrado."));

		mockMvc.perform(get("/projectTypes/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Tipo de proyecto no encontrado."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(projectTypeService.getProjectTypeDetail(1L))
			.thenReturn(new ProjectTypeDetailDto(1L, "Terminaciones", true, null, null));

		mockMvc.perform(put("/projectTypes/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Terminaciones\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Terminaciones"));
	}

	@Test
	void toggleActive_retorna200ConFlagAlternado() throws Exception {
		when(projectTypeService.toggleProjectTypeActive(1L))
			.thenReturn(new ProjectTypeDetailDto(1L, "Obra gruesa", false, null, null));

		mockMvc.perform(patch("/projectTypes/1/active").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(false));
	}

	// --- Autorizacion por permiso (@PreAuthorize) ---
	// Cada endpoint exige su authority exacta: sin permiso -> 403 con el shape del contrato; con el
	// permiso exacto -> 2xx. create_conPermisoDeOtraAccion prueba que no basta con tener "algun"
	// permiso de projectType.

	@Test
	void list_sinPermiso_retorna403ConShapeDeContrato() throws Exception {
		mockMvc.perform(get("/projectTypes").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403))
			.andExpect(jsonPath("$.error.name").value("FORBIDDEN"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void detail_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/projectTypes/1").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/projectTypes")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Obra gruesa\"}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void update_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(put("/projectTypes/1")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Terminaciones\"}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void toggleActive_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(patch("/projectTypes/1/active").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_conPermisoDeOtraAccion_retorna403() throws Exception {
		mockMvc.perform(post("/projectTypes")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectType.read"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Obra gruesa\"}"))
			.andExpect(status().isForbidden());
	}

	@Test
	void list_conPermisoExacto_retorna200() throws Exception {
		when(projectTypeService.listProjectTypes(any(), any())).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/projectTypes").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectType.read")))
			.andExpect(status().isOk());
	}

	@Test
	void detail_conPermisoExacto_retorna200() throws Exception {
		when(projectTypeService.getProjectTypeDetail(1L))
			.thenReturn(new ProjectTypeDetailDto(1L, "Obra gruesa", true, null, null));

		mockMvc.perform(get("/projectTypes/1").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectType.read")))
			.andExpect(status().isOk());
	}

	@Test
	void create_conPermisoExacto_retorna201() throws Exception {
		when(projectTypeService.createProjectType(any())).thenReturn(10L);

		mockMvc.perform(post("/projectTypes")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectType.create"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Obra gruesa\"}"))
			.andExpect(status().isCreated());
	}

	@Test
	void update_conPermisoExacto_retorna200() throws Exception {
		when(projectTypeService.getProjectTypeDetail(1L))
			.thenReturn(new ProjectTypeDetailDto(1L, "Terminaciones", true, null, null));

		mockMvc.perform(put("/projectTypes/1")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectType.update"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Terminaciones\"}"))
			.andExpect(status().isOk());
	}

	@Test
	void toggleActive_conPermisoExacto_retorna200() throws Exception {
		when(projectTypeService.toggleProjectTypeActive(1L))
			.thenReturn(new ProjectTypeDetailDto(1L, "Obra gruesa", false, null, null));

		mockMvc.perform(patch("/projectTypes/1/active")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("projectType.active")))
			.andExpect(status().isOk());
	}
}
