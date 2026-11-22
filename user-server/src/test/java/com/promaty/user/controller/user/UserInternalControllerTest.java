package com.promaty.user.controller.user;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.promaty.contracts.auth.InternalApiHeader;
import com.promaty.contracts.auth.UserSessionStatusDto;
import com.promaty.user.config.SecurityConfig;
import com.promaty.user.exception.ResourceNotFoundException;
import com.promaty.user.services.user.UserService;
import com.promaty.user.support.TestInternalApi;
import com.promaty.user.support.TestJwt;

@WebMvcTest(UserInternalController.class)
@Import(SecurityConfig.class)
@EnableWebSecurity
@TestPropertySource(properties = { "jwt.secret=" + TestJwt.SECRET, "internal-api.secret=" + TestInternalApi.SECRET })
class UserInternalControllerTest {

	private static final String SESSION_URL = "/internal/users/3/session";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	void getSessionStatus_conSecretoDeServicio_retorna200ConActivoYVersion() throws Exception {
		when(userService.getSessionStatus(3L)).thenReturn(new UserSessionStatusDto(true, 4L));

		mockMvc.perform(get(SESSION_URL).header(InternalApiHeader.SECRET, TestInternalApi.SECRET))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.active").value(true))
			.andExpect(jsonPath("$.data.sessionVersion").value(4));
	}

	@Test
	void getSessionStatus_usuarioNoExiste_retorna404() throws Exception {
		when(userService.getSessionStatus(3L)).thenThrow(new ResourceNotFoundException("Usuario no encontrado."));

		mockMvc.perform(get(SESSION_URL).header(InternalApiHeader.SECRET, TestInternalApi.SECRET))
			.andExpect(status().isNotFound());
	}

	@Test
	void getSessionStatus_sinSecretoDeServicio_retorna401YNoLlegaAlService() throws Exception {
		mockMvc.perform(get(SESSION_URL))
			.andExpect(status().isUnauthorized());

		verifyNoInteractions(userService);
	}

	@Test
	void getSessionStatus_conTokenDeUsuarioPeroSinSecreto_retorna403() throws Exception {
		mockMvc.perform(get(SESSION_URL).header(HttpHeaders.AUTHORIZATION, TestJwt.bearer("user.read")))
			.andExpect(status().isForbidden());

		verifyNoInteractions(userService);
	}
}
