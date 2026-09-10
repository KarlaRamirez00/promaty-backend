package com.promaty.rrhh.controller.client;

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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.dto.client.ClientDetailDto;
import com.promaty.rrhh.dto.client.ClientListDto;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.services.client.ClientService;

// @WebMvcTest en Boot 4.1 no auto-configura Spring Security; @EnableWebSecurity restituye el
// bean HttpSecurity que SecurityConfig necesita.
@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
class ClientControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ClientService clientService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(clientService.createClient(any())).thenReturn(10L);

		mockMvc.perform(post("/clients")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Sodimac\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10))
			.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void create_conNombreEnBlanco_retorna400ConErrorFields() throws Exception {
		mockMvc.perform(post("/clients")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\" \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.error.errorFields.name").exists())
			.andExpect(jsonPath("$.data").doesNotExist());
	}

	@Test
	void list_retorna200ConDataYPaginacion() throws Exception {
		Page<ClientListDto> pagina = new PageImpl<>(
			List.of(new ClientListDto(1L, "Sodimac", true, LocalDateTime.of(2026, 1, 15, 10, 0), null)),
			PageRequest.of(0, 20),
			1
		);
		when(clientService.listClients(any(), any())).thenReturn(pagina);

		mockMvc.perform(get("/clients"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[0].name").value("Sodimac"))
			.andExpect(jsonPath("$.data[0].createdAt").exists())
			.andExpect(jsonPath("$.meta.pagination.total").value(1));
	}

	@Test
	void detail_registroExiste_retorna200ConData() throws Exception {
		when(clientService.getClientDetail(1L))
			.thenReturn(new ClientDetailDto(1L, "Sodimac", true, null, null));

		mockMvc.perform(get("/clients/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Sodimac"));
	}

	@Test
	void detail_registroNoExiste_retorna404() throws Exception {
		when(clientService.getClientDetail(99L))
			.thenThrow(new ResourceNotFoundException("Mandante no encontrado."));

		mockMvc.perform(get("/clients/99"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.error.message").value("Mandante no encontrado."));
	}

	@Test
	void update_conDatosValidos_retorna200ConDetalleActualizado() throws Exception {
		when(clientService.getClientDetail(1L))
			.thenReturn(new ClientDetailDto(1L, "Falabella", true, null, null));

		mockMvc.perform(put("/clients/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Falabella\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.name").value("Falabella"));
	}

	@Test
	void toggleActive_retorna200ConFlagAlternado() throws Exception {
		when(clientService.toggleClientActive(1L))
			.thenReturn(new ClientDetailDto(1L, "Sodimac", false, null, null));

		mockMvc.perform(patch("/clients/1/active"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(false));
	}
}
