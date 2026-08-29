package com.promaty.user.services.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.promaty.contracts.auth.AuthValidationRequestDto;
import com.promaty.contracts.auth.AuthValidationResponseDto;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.repository.UserProjectAccessRepository;
import com.promaty.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserProjectAccessRepository userProjectAccessRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthServiceImpl authService;

	@Test
	void validate_conCredencialesValidas_retornaValidoConDatosDelUsuario() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		User user = usuarioActivo();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);
		when(userProjectAccessRepository.findProjectIdsByUserId(1L)).thenReturn(List.of(10L, 20L));

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getValid()).isTrue();
		assertThat(resultado.getUserId()).isEqualTo(1L);
		assertThat(resultado.getRole()).isEqualTo("Editor");
		assertThat(resultado.getPermissions()).containsExactly("warehouse.read");
		assertThat(resultado.getAllowedAllProjects()).isFalse();
		assertThat(resultado.getProjectIds()).containsExactly(10L, 20L);
	}

	@Test
	void validate_conPermisoAllowedAll_retornaAllowedAllProjectsTrueYNoConsultaProyectos() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		User user = usuarioActivo();
		Permission allowedAll = new Permission();
		allowedAll.setName("warehouse.AllowedAll");
		user.getRole().setPermissions(Set.of(allowedAll));
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getAllowedAllProjects()).isTrue();
		assertThat(resultado.getProjectIds()).isEmpty();
		verify(userProjectAccessRepository, never()).findProjectIdsByUserId(eq(1L));
	}

	@Test
	void validate_conEmailInexistente_retornaInvalido() {
		AuthValidationRequestDto dto = requestDto("noexiste@promaty.com", "clave123");
		when(userRepository.findByEmail("noexiste@promaty.com")).thenReturn(Optional.empty());

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getValid()).isFalse();
		assertThat(resultado.getUserId()).isNull();
	}

	@Test
	void validate_conPasswordIncorrecta_retornaInvalido() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "claveIncorrecta");
		User user = usuarioActivo();
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("claveIncorrecta", user.getPassword())).thenReturn(false);

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getValid()).isFalse();
	}

	@Test
	void validate_conUsuarioInactivo_retornaInvalido() {
		AuthValidationRequestDto dto = requestDto("ana@promaty.com", "clave123");
		User user = usuarioActivo();
		user.setActive(false);
		when(userRepository.findByEmail("ana@promaty.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("clave123", user.getPassword())).thenReturn(true);

		AuthValidationResponseDto resultado = authService.validate(dto);

		assertThat(resultado.getValid()).isFalse();
	}

	private AuthValidationRequestDto requestDto(String email, String password) {
		AuthValidationRequestDto dto = new AuthValidationRequestDto();
		dto.setEmail(email);
		dto.setPassword(password);
		return dto;
	}

	private User usuarioActivo() {
		Permission permiso = new Permission();
		permiso.setName("warehouse.read");

		Role role = new Role();
		role.setName("Editor");
		role.setPermissions(Set.of(permiso));

		User user = new User();
		user.setId(1L);
		user.setEmail("ana@promaty.com");
		user.setPassword("$2a$10$hashSimulado");
		user.setActive(true);
		user.setRole(role);
		return user;
	}
}
