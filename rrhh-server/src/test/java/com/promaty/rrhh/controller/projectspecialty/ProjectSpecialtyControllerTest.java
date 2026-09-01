package com.promaty.rrhh.controller.projectspecialty;

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
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyDetailDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyListDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.projectspecialty.ProjectSpecialtyService;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(ProjectSpecialtyController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
class ProjectSpecialtyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProjectSpecialtyService projectSpecialtyService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(projectSpecialtyService.createProjectSpecialty(any())).thenReturn(10L);

		mockMvc.perform(post("/projectSpecialties")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Eléctrica\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_conNombreEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/projectSpecialties")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.name").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void list_retorna200ConDataYPaginacion() throws Exception {
		Page<ProjectSpecialtyListDto> pagina = new PageImpl<>(
			List.of(new ProjectSpecialtyListDto(1L, "Eléctrica", true)),
			PageRequest.of(0, 20),
			1
		);
		when(projectSpecialtyService.listProjectSpecialties(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/projectSpecialties"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Eléctrica"))
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_registroExiste_retorna200ConData() throws Exception {
		when(projectSpecialtyService.getProjectSpecialtyDetail(1L))
			.thenReturn(new ProjectSpecialtyDetailDto(1L, "Eléctrica", true, null, null));

		mockMvc.perform(get("/projectSpecialties/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Eléctrica"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(projectSpecialtyService.getProjectSpecialtyDetail(99L))
			.thenThrow(new ResourceNotFoundException("Especialidad no encontrada."));

		mockMvc.perform(get("/projectSpecialties/99"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Especialidad no encontrada."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(projectSpecialtyService.getProjectSpecialtyDetail(1L))
			.thenReturn(new ProjectSpecialtyDetailDto(1L, "Sanitaria", true, null, null));

		mockMvc.perform(put("/projectSpecialties/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Sanitaria\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Sanitaria"));
	}

	@Test
	void toggleActive_retorna200ConFlagAlternado() throws Exception {
		when(projectSpecialtyService.toggleProjectSpecialtyActive(1L))
			.thenReturn(new ProjectSpecialtyDetailDto(1L, "Eléctrica", false, null, null));

		mockMvc.perform(patch("/projectSpecialties/1/active"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(false));
	}
}
