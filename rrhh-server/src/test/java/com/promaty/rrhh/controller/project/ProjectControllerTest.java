package com.promaty.rrhh.controller.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.dto.project.ProjectDetailDto;
import com.promaty.rrhh.dto.project.ProjectListDto;
import com.promaty.rrhh.dto.project.RelationSummaryDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.project.ProjectService;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(ProjectController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
class ProjectControllerTest {

	private static final String PROYECTO_JSON =
		"{\"name\":\"Edificio Norte\",\"costCenterCode\":\"00824\",\"typeId\":10,\"specialtyId\":20,"
			+ "\"clientId\":30,\"statusId\":40,\"startDate\":\"2026-01-01\"}";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProjectService projectService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(projectService.createProject(any())).thenReturn(10L);

		mockMvc.perform(post("/projects")
				.contentType(MediaType.APPLICATION_JSON)
				.content(PROYECTO_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_sinCamposObligatorios_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/projects")
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
				LocalDate.of(2026, 1, 1), null)),
			PageRequest.of(0, 20),
			1
		);
		when(projectService.listProjects(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/projects"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Edificio Norte"))
			.andExpect(jsonPath("$.data[0].statusName").value("En ejecución"))
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_registroExiste_retorna200ConData() throws Exception {
		when(projectService.getProjectDetail(1L)).thenReturn(detalleDto());

		mockMvc.perform(get("/projects/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Edificio Norte"))
			.andExpect(jsonPath("$.data.type.name").value("Obra gruesa"))
			.andExpect(jsonPath("$.data.status.code").value("IN_PROGRESS"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(projectService.getProjectDetail(99L))
			.thenThrow(new ResourceNotFoundException("Proyecto no encontrado."));

		mockMvc.perform(get("/projects/99"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Proyecto no encontrado."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(projectService.getProjectDetail(1L)).thenReturn(detalleDto());

		mockMvc.perform(put("/projects/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(PROYECTO_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Edificio Norte"));
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
			null
		);
	}
}
