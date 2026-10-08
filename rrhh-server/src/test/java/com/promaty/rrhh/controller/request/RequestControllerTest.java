package com.promaty.rrhh.controller.request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.rrhh.config.SecurityConfig;
import com.promaty.rrhh.services.request.RequestService;
import com.promaty.rrhh.support.TestJwt;

@WebMvcTest(RequestController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = "jwt.secret=" + TestJwt.SECRET)
class RequestControllerTest {

	private static final String TOKEN = TestJwt.bearer("contract.create");
	private static final String SIN_PERMISOS = TestJwt.bearer();

	private static final String REQUEST_JSON = """
		{
		  "entityType": "CONTRACT",
		  "action": "CREATE",
		  "projectId": 1,
		  "contractData": {
		    "staffId": 2,
		    "companyId": 3,
		    "contractTypeId": 4,
		    "jobTitleId": 5,
		    "siteId": 6,
		    "startDate": "2026-01-01",
		    "baseSalary": 850000,
		    "weeklyWorkHours": 45,
		    "workDays": 5
		  }
		}
		""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RequestService requestService;

	@Test
	void create_conDatosValidos_retorna201ConId() throws Exception {
		when(requestService.createRequest(any())).thenReturn(10L);

		mockMvc.perform(post("/requests")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(REQUEST_JSON))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.data").value(10));
	}

	@Test
	void create_sinProjectId_retorna400() throws Exception {
		mockMvc.perform(post("/requests")
				.header(HttpHeaders.AUTHORIZATION, TOKEN)
				.contentType(MediaType.APPLICATION_JSON)
				.content(REQUEST_JSON.replace("\"projectId\": 1,", "")))
			.andExpect(status().isBadRequest());
	}

	@Test
	void create_sinPermiso_retorna403() throws Exception {
		mockMvc.perform(post("/requests")
				.header(HttpHeaders.AUTHORIZATION, SIN_PERMISOS)
				.contentType(MediaType.APPLICATION_JSON)
				.content(REQUEST_JSON))
			.andExpect(status().isForbidden());
	}
}
