package com.promaty.rrhh.controller.projecttype;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.promaty.rrhh.dto.projecttype.ProjectTypeDetailDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeListDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.projecttype.ProjectTypeService;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(ProjectTypeController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
class ProjectTypeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProjectTypeService projectTypeService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(projectTypeService.createProjectType(any())).thenReturn(10L);

		mockMvc.perform(post("/projectTypes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Obra gruesa\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_conNombreEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/projectTypes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.name").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void list_retorna200ConDataYPaginacion() throws Exception {
		Page<ProjectTypeListDto> pagina = new PageImpl<>(
			List.of(new ProjectTypeListDto(1L, "Obra gruesa", true)),
			PageRequest.of(0, 20),
			1
		);
		when(projectTypeService.listProjectTypes(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/projectTypes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Obra gruesa"))
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_registroExiste_retorna200ConData() throws Exception {
		when(projectTypeService.getProjectTypeDetail(1L))
			.thenReturn(new ProjectTypeDetailDto(1L, "Obra gruesa", true, null, null));

		mockMvc.perform(get("/projectTypes/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Obra gruesa"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(projectTypeService.getProjectTypeDetail(99L))
			.thenThrow(new ResourceNotFoundException("Tipo de proyecto no encontrado."));

		mockMvc.perform(get("/projectTypes/99"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Tipo de proyecto no encontrado."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(projectTypeService.getProjectTypeDetail(1L))
			.thenReturn(new ProjectTypeDetailDto(1L, "Terminaciones", true, null, null));

		mockMvc.perform(put("/projectTypes/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Terminaciones\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Terminaciones"));
	}

	@Test
	void toggleActive_retorna200ConFlagAlternado() throws Exception {
		when(projectTypeService.toggleProjectTypeActive(1L))
			.thenReturn(new ProjectTypeDetailDto(1L, "Obra gruesa", false, null, null));

		mockMvc.perform(patch("/projectTypes/1/active"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(false));
	}
}
