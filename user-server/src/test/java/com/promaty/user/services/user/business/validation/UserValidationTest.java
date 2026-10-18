package com.promaty.user.services.user.business.validation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.promaty.user.dto.user.CreateUserDto;
import com.promaty.user.dto.user.UpdateUserDto;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.exception.BusinessValidationException;
import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserValidationTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@InjectMocks
	private UserValidation userValidation;

	@AfterEach
	void limpiarContextoDeSeguridad() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void validateCreate_conRolNoSistema_noLanza() {
		when(userRepository.existsByEmail("ana@promaty.cl")).thenReturn(false);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(rol(1L, false)));

		assertThatNoException().isThrownBy(() -> userValidation.validateCreate(dtoCreate("ana@promaty.cl", 1L)));
	}

	@Test
	void validateCreate_conRolInexistente_lanzaConErrorFields() {
		when(userRepository.existsByEmail("ana@promaty.cl")).thenReturn(false);
		when(roleRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userValidation.validateCreate(dtoCreate("ana@promaty.cl", 99L)))
			.isInstanceOf(BusinessValidationException.class);
	}

	@Test
	void validateCreate_conRolDeSistemaYSinPermiso_lanza() {
		when(userRepository.existsByEmail("ana@promaty.cl")).thenReturn(false);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(rol(1L, true)));
		autenticarCon();

		assertThatThrownBy(() -> userValidation.validateCreate(dtoCreate("ana@promaty.cl", 1L)))
			.isInstanceOf(BusinessValidationException.class);
	}

	@Test
	void validateCreate_conRolDeSistemaYConPermiso_noLanza() {
		when(userRepository.existsByEmail("ana@promaty.cl")).thenReturn(false);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(rol(1L, true)));
		autenticarCon("user.assignSystemRole");

		assertThatNoException().isThrownBy(() -> userValidation.validateCreate(dtoCreate("ana@promaty.cl", 1L)));
	}

	@Test
	void validateUpdate_conEmailDeOtroUsuario_lanza() {
		when(roleRepository.findById(1L)).thenReturn(Optional.of(rol(1L, false)));
		User otroUsuario = new User();
		otroUsuario.setId(2L);
		when(userRepository.findByEmail("ana@promaty.cl")).thenReturn(Optional.of(otroUsuario));

		assertThatThrownBy(() -> userValidation.validateUpdate(1L, dtoUpdate("ana@promaty.cl", 1L)))
			.isInstanceOf(BusinessValidationException.class);
	}

	@Test
	void validateUpdate_conRolDeSistemaYSinPermiso_lanza() {
		when(roleRepository.findById(1L)).thenReturn(Optional.of(rol(1L, true)));
		when(userRepository.findByEmail("ana@promaty.cl")).thenReturn(Optional.empty());
		autenticarCon();

		assertThatThrownBy(() -> userValidation.validateUpdate(1L, dtoUpdate("ana@promaty.cl", 1L)))
			.isInstanceOf(BusinessValidationException.class);
	}

	private void autenticarCon(String... authorities) {
		List<SimpleGrantedAuthority> grantedAuthorities = List.of(authorities).stream()
			.map(SimpleGrantedAuthority::new)
			.toList();
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken("test", null, grantedAuthorities));
	}

	private Role rol(Long id, boolean esDeSistema) {
		Role rol = new Role();
		rol.setId(id);
		rol.setName("Superadmin");
		rol.setSystem(esDeSistema);
		return rol;
	}

	private CreateUserDto dtoCreate(String email, Long roleId) {
		CreateUserDto dto = new CreateUserDto();
		dto.setFirstName("Ana");
		dto.setLastName("Perez");
		dto.setEmail(email);
		dto.setPassword("claveSegura123");
		dto.setRoleId(roleId);
		return dto;
	}

	private UpdateUserDto dtoUpdate(String email, Long roleId) {
		UpdateUserDto dto = new UpdateUserDto();
		dto.setFirstName("Ana");
		dto.setLastName("Perez");
		dto.setEmail(email);
		dto.setRoleId(roleId);
		return dto;
	}
}
