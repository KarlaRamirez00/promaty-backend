package com.promaty.rrhh.services.request.business.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.services.request.handler.RequestHandler;
import com.promaty.security.JwtPrincipal;

@ExtendWith(MockitoExtension.class)
class RequestSyncRetryValidationTest {

	private static final String VALIDATE = "contract.validate";

	@Mock
	private RequestHandler handler;

	private final RequestSyncRetryValidation validation = new RequestSyncRetryValidation();

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void validate_conPermisoAccesoYRegistroEnSyncError_noLanzaExcepcion() {
		autenticar(false, List.of(7L), VALIDATE);
		when(handler.validatePermission()).thenReturn(VALIDATE);
		when(handler.isInSyncError(100L)).thenReturn(true);

		assertThatCode(() -> validation.validate(solicitudDelProyecto(7L), handler)).doesNotThrowAnyException();
	}

	@Test
	void validate_sinElPermisoDeValidarDeEseTipo_lanzaAccessDeniedSinConsultarLaEntidad() {
		autenticar(true, List.of(), "termination.validate");
		when(handler.validatePermission()).thenReturn(VALIDATE);

		assertThatThrownBy(() -> validation.validate(solicitudDelProyecto(7L), handler))
			.isInstanceOf(AccessDeniedException.class);

		verify(handler).validatePermission();
		verifyNoMoreInteractions(handler);
	}

	@Test
	void validate_fueraDeSuCentroDeCosto_lanzaAccessDenied() {
		autenticar(false, List.of(8L), VALIDATE);
		when(handler.validatePermission()).thenReturn(VALIDATE);

		assertThatThrownBy(() -> validation.validate(solicitudDelProyecto(7L), handler))
			.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void validate_registroQueNoEstaEnSyncError_lanzaErrorEnStatus() {
		autenticar(true, List.of(), VALIDATE);
		when(handler.validatePermission()).thenReturn(VALIDATE);
		when(handler.isInSyncError(100L)).thenReturn(false);

		assertThatThrownBy(() -> validation.validate(solicitudDelProyecto(7L), handler))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields()).containsKey("status"));
	}

	private Request solicitudDelProyecto(Long projectId) {
		Project project = new Project();
		project.setId(projectId);
		Request request = new Request();
		request.setEntityId(100L);
		request.setProject(project);
		return request;
	}

	private void autenticar(boolean allowedAllProjects, List<Long> projectIds, String permiso) {
		JwtPrincipal principal = new JwtPrincipal(
			"9", "Rol", "Usuario", List.of(permiso), allowedAllProjects, projectIds, false, 0L);
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority(permiso))));
	}
}
