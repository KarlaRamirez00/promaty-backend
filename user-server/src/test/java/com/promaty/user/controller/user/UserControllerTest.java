package com.promaty.user.controller.user;

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
import com.promaty.user.dto.role.RoleSummaryDto;
import com.promaty.user.dto.user.CreateUserDto;
import com.promaty.user.dto.user.UpdateUserDto;
import com.promaty.user.dto.user.UserDetailDto;
import com.promaty.user.dto.user.UserListDto;
import com.promaty.user.exception.ResourceNotFoundException;
import com.promaty.user.services.user.UserService;
import com.promaty.user.support.TestJwt;

// Mismo setup que RoleControllerTest: Bearer real (TestJwt) porque en Boot 4.1 @WebMvcTest no
// auto-configura spring-security-test, y @EnableWebSecurity porque el slice no trae HttpSecurity.
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class UserControllerTest {

	// Token con todos los permisos de user: los tests de flujo funcional no ejercen la autorizacion
	// (eso vive en la seccion "Autorizacion por permiso" mas abajo).
	private static final String TOKEN = TestJwt.bearer("user.read", "user.create", "user.update", "user.active");

	// Token valido pero sin ninguna authority.
	private static final String SIN_PERMISOS = TestJwt.bearer();

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	private CreateUserDto createDtoValido() {
		CreateUserDto dto = new CreateUserDto();
		dto.setFirstName("Ana");
		dto.setLastName("Perez");
		dto.setEmail("ana.perez@promaty.cl");
		dto.setPassword("clave1234");
		dto.setRoleId(1L);
		return dto;
	}

	private UpdateUserDto updateDtoValido() {
		UpdateUserDto dto = new UpdateUserDto();
		dto.setFirstName("Ana");
		dto.setLastName("Perez");
		dto.setEmail("ana.perez@promaty.cl");
		dto.setRoleId(1L);
		return dto;
	}

	private UserDetailDto detalle() {
		return new UserDetailDto(1L, "Ana", "Perez", "ana.perez@promaty.cl", null, true,
			new RoleSummaryDto(1L, "Editor"), null, null, "system", null);
	}

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(userService.createUser(any())).thenReturn(10L);

		mockMvc.perform(post("/users")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDtoValido())))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_conEmailInvalido_retorna400ConErrorFields() throws Exception {
		CreateUserDto dto = createDtoValido();
		dto.setEmail("no-es-un-email");

		mockMvc.perform(post("/users")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.email").exists());
	}

	@Test
	void list_retorna200ConDataYPaginacion() throws Exception {
		UserListDto usuario = new UserListDto(1L, "Ana", "Perez", "ana.perez@promaty.cl", true,
			new RoleSummaryDto(1L, "Editor"), null, null, "system", null);
		Page<UserListDto> pagina = new PageImpl<>(List.of(usuario), PageRequest.of(0, 20), 1);
		when(userService.listUsers(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].email").value("ana.perez@promaty.cl"))
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_usuarioExiste_retorna200ConData() throws Exception {
		when(userService.getUserDetail(1L)).thenReturn(detalle());

		mockMvc.perform(get("/users/1").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.firstName").value("Ana"));
	}

	@Test
	void detail_usuarioNoExiste_retorna404() throws Exception {
		when(userService.getUserDetail(99L)).thenThrow(new ResourceNotFoundException("Usuario no encontrado."));

		mockMvc.perform(get("/users/99").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Usuario no encontrado."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(userService.getUserDetail(1L)).thenReturn(detalle());

		mockMvc.perform(put("/users/1")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDtoValido())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.email").value("ana.perez@promaty.cl"));
	}

	@Test
	void toggleActive_retorna200ConResultado() throws Exception {
		UserDetailDto desactivado = detalle();
		desactivado.setActive(false);
		when(userService.toggleUserActive(anyLong())).thenReturn(desactivado);

		mockMvc.perform(patch("/users/1/active").header(HttpHeaders.AUTHORIZATION, TOKEN))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(false));
	}

	// --- Autorizacion por permiso (@PreAuthorize) ---
	// Cada endpoint exige su authority exacta: sin permiso -> 403 con el shape del contrato; con el
	// permiso exacto -> 2xx. create_conPermisoDeOtraAccion prueba que no basta con tener "algun"
	// permiso de user.

	@Test
	void list_sinPermiso_retorna403ConShapeDeContrato() throws Exception {
		mockMvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403))
			.andExpect(jsonPath("$.error.name").value("FORBIDDEN"))
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void detail_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(get("/users/1").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/users")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDtoValido())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void update_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(put("/users/1")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDtoValido())))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void toggleActive_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(patch("/users/1/active").header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.error.status").value(403));
	}

	@Test
	void create_conPermisoDeOtraAccion_retorna403() throws Exception {
		mockMvc.perform(post("/users")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("user.read"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDtoValido())))
			.andExpect(status().isForbidden());
	}

	@Test
	void list_conPermisoExacto_retorna200() throws Exception {
		when(userService.listUsers(any(), any())).thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("user.read")))
			.andExpect(status().isOk());
	}

	@Test
	void detail_conPermisoExacto_retorna200() throws Exception {
		when(userService.getUserDetail(1L)).thenReturn(detalle());

		mockMvc.perform(get("/users/1").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("user.read")))
			.andExpect(status().isOk());
	}

	@Test
	void create_conPermisoExacto_retorna201() throws Exception {
		when(userService.createUser(any())).thenReturn(10L);

		mockMvc.perform(post("/users")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("user.create"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createDtoValido())))
			.andExpect(status().isCreated());
	}

	@Test
	void update_conPermisoExacto_retorna200() throws Exception {
		when(userService.getUserDetail(1L)).thenReturn(detalle());

		mockMvc.perform(put("/users/1")
				.header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("user.update"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateDtoValido())))
			.andExpect(status().isOk());
	}

	@Test
	void toggleActive_conPermisoExacto_retorna200() throws Exception {
		when(userService.toggleUserActive(anyLong())).thenReturn(detalle());

		mockMvc.perform(patch("/users/1/active").header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("user.active")))
			.andExpect(status().isOk());
	}
}
