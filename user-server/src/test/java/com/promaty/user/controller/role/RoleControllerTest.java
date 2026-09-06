package com.promaty.user.controller.role;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.user.config.SecurityConfig;
import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.dto.role.RoleActiveUpdateResultDto;
import com.promaty.user.dto.role.RoleDetailDto;
import com.promaty.user.dto.role.RoleListDto;
import com.promaty.user.dto.role.UpdateRoleDto;
import com.promaty.user.exception.ResourceNotFoundException;
import com.promaty.user.services.role.RoleService;
import com.promaty.user.support.TestJwt;

// Cada llamada lleva un Bearer real (TestJwt): en Boot 4.1 @WebMvcTest no auto-configura la
// integracion de spring-security-test, asi que @WithMockUser no surtiria efecto. @EnableWebSecurity
// tambien es necesario por lo mismo (no trae el bean HttpSecurity por defecto).
@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class RoleControllerTest {

	private static final String TOKEN = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private RoleService roleService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		CreateRoleDto dto = new CreateRoleDto();
		dto.setName("Editor");
		when(roleService.createRole(any())).thenReturn(10L);

		mockMvc.perform(post("/roles")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_conNombreEnBlanco_retorna400ConErrorFields() throws Exception {
		CreateRoleDto dto = new CreateRoleDto();
		dto.setName(" ");

		mockMvc.perform(post("/roles")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.name").exists());
	}

	@Test
	void list_retorna200ConDataYPaginacion() throws Exception {
		RoleListDto rol = new RoleListDto(1L, "Editor", "desc", true, 0L);
		Page<RoleListDto> pagina = new PageImpl<>(List.of(rol), PageRequest.of(0, 20), 1);
		when(roleService.listRoles(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/roles").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Editor"))
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_rolExiste_retorna200ConData() throws Exception {
		RoleDetailDto detalle = new RoleDetailDto(1L, "Editor", "desc", true, 0L, List.of(), List.of());
		when(roleService.getRoleDetail(1L)).thenReturn(detalle);

		mockMvc.perform(get("/roles/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Editor"));
	}

	@Test
	void detail_rolNoExiste_retorna404() throws Exception {
		when(roleService.getRoleDetail(99L)).thenThrow(new ResourceNotFoundException("Rol no encontrado."));

		mockMvc.perform(get("/roles/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Rol no encontrado."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		UpdateRoleDto dto = new UpdateRoleDto();
		dto.setName("Editor actualizado");
		RoleDetailDto detalle = new RoleDetailDto(1L, "Editor actualizado", "desc", true, 0L, List.of(), List.of());
		when(roleService.getRoleDetail(1L)).thenReturn(detalle);

		mockMvc.perform(put("/roles/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Editor actualizado"));
	}

	@Test
	void toggleActive_retorna200ConResultado() throws Exception {
		RoleActiveUpdateResultDto resultado = new RoleActiveUpdateResultDto(1L, false, 3L);
		when(roleService.toggleRoleActive(anyLong(), any())).thenReturn(resultado);

		mockMvc.perform(patch("/roles/1/active")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(false))
			.andExpect(jsonPath("$.data.reassignedUsers").value(3));
	}
}
