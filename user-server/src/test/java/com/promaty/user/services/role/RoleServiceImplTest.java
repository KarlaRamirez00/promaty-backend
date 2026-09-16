package com.promaty.user.services.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.dto.role.RoleActiveUpdateDto;
import com.promaty.user.dto.role.RoleActiveUpdateResultDto;
import com.promaty.user.dto.role.UpdateRoleDto;
import com.promaty.user.dto.shared.Action;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.exception.BusinessValidationException;
import com.promaty.user.exception.ResourceNotFoundException;
import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.UserRepository;
import com.promaty.user.services.role.business.builder.CreateRoleBuilder;
import com.promaty.user.services.role.business.builder.RoleRelationsResolver;
import com.promaty.user.services.role.business.validation.RoleValidation;
import com.promaty.user.services.shared.ActionsResolver;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleValidation roleValidation;

	@Mock
	private CreateRoleBuilder createRoleBuilder;

	@Mock
	private RoleRelationsResolver relationsResolver;

	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private RoleServiceImpl roleService;

	@Test
	void createRole_conDatosValidos_guardaYRetornaId() {
		CreateRoleDto dto = new CreateRoleDto();
		Role role = new Role();
		Role guardado = roleConId(5L, true);
		when(createRoleBuilder.build(dto)).thenReturn(role);
		when(roleRepository.save(role)).thenReturn(guardado);

		Long id = roleService.createRole(dto);

		assertThat(id).isEqualTo(5L);
		verify(roleValidation).validateCreate(dto);
	}

	@Test
	void updateRole_rolNoExiste_lanzaResourceNotFoundException() {
		UpdateRoleDto dto = new UpdateRoleDto();
		when(roleRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> roleService.updateRole(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getRoleDetail_rolNoExiste_lanzaResourceNotFoundException() {
		when(roleRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> roleService.getRoleDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void updateRole_esRolDeSistema_lanzaBusinessValidationExceptionSinLlegarAValidar() {
		Role role = roleConId(1L, true);
		role.setSystem(true);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

		assertThatThrownBy(() -> roleService.updateRole(1L, new UpdateRoleDto()))
			.isInstanceOf(BusinessValidationException.class);
		verify(roleValidation, never()).validateUpdate(any(), any());
	}

	@Test
	void toggleRoleActive_esRolDeSistema_lanzaBusinessValidationException() {
		Role role = roleConId(1L, true);
		role.setSystem(true);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

		assertThatThrownBy(() -> roleService.toggleRoleActive(1L, new RoleActiveUpdateDto()))
			.isInstanceOf(BusinessValidationException.class);
		verify(roleRepository, never()).save(any());
	}

	@Test
	void getRoleDetail_esRolDeSistema_excluyeUpdateYActiveDeActions() {
		Role role = roleConId(1L, true);
		role.setSystem(true);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
		when(userRepository.countByRole_Id(1L)).thenReturn(0L);
		when(actionsResolver.resolve(any(), any()))
			.thenReturn(List.of(Action.READ, Action.UPDATE, Action.ACTIVE));

		var detalle = roleService.getRoleDetail(1L);

		assertThat(detalle.getActions()).containsExactly(Action.READ);
	}

	@Test
	void getRoleDetail_noEsRolDeSistema_mantieneTodasLasActions() {
		Role role = roleConId(1L, true);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
		when(userRepository.countByRole_Id(1L)).thenReturn(0L);
		when(actionsResolver.resolve(any(), any()))
			.thenReturn(List.of(Action.READ, Action.UPDATE, Action.ACTIVE));

		var detalle = roleService.getRoleDetail(1L);

		assertThat(detalle.getActions()).containsExactly(Action.READ, Action.UPDATE, Action.ACTIVE);
	}

	@Test
	void toggleRoleActive_activoSinUsuariosAsignados_desactivaSinReasignar() {
		Role role = roleConId(1L, true);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
		when(userRepository.findByRole_Id(1L)).thenReturn(List.of());
		when(roleRepository.save(role)).thenReturn(role);

		RoleActiveUpdateResultDto resultado = roleService.toggleRoleActive(1L, new RoleActiveUpdateDto());

		assertThat(resultado.getActive()).isFalse();
		assertThat(resultado.getReassignedUsers()).isZero();
		verify(userRepository, never()).saveAll(any());
	}

	@Test
	void toggleRoleActive_activoConUsuariosSinNewRoleId_lanzaBusinessValidationException() {
		Role role = roleConId(1L, true);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
		when(userRepository.findByRole_Id(1L)).thenReturn(List.of(new User()));

		assertThatThrownBy(() -> roleService.toggleRoleActive(1L, new RoleActiveUpdateDto()))
			.isInstanceOf(BusinessValidationException.class)
			.satisfies(ex -> assertThat(((BusinessValidationException) ex).getErrorFields())
				.containsKey("newRoleId"));
	}

	@Test
	void toggleRoleActive_activoConUsuariosYNewRoleIdInexistente_lanzaResourceNotFoundException() {
		Role role = roleConId(1L, true);
		RoleActiveUpdateDto dto = new RoleActiveUpdateDto();
		dto.setNewRoleId(99L);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
		when(userRepository.findByRole_Id(1L)).thenReturn(List.of(new User()));
		when(roleRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> roleService.toggleRoleActive(1L, dto))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void toggleRoleActive_activoConUsuariosYNewRoleIdValido_reasignaYRetornaConteo() {
		Role role = roleConId(1L, true);
		Role rolReemplazo = roleConId(2L, true);
		RoleActiveUpdateDto dto = new RoleActiveUpdateDto();
		dto.setNewRoleId(2L);
		User usuario1 = new User();
		User usuario2 = new User();
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
		when(userRepository.findByRole_Id(1L)).thenReturn(List.of(usuario1, usuario2));
		when(roleRepository.findById(2L)).thenReturn(Optional.of(rolReemplazo));
		when(roleRepository.save(role)).thenReturn(role);

		RoleActiveUpdateResultDto resultado = roleService.toggleRoleActive(1L, dto);

		assertThat(resultado.getReassignedUsers()).isEqualTo(2L);
		assertThat(usuario1.getRole()).isEqualTo(rolReemplazo);
		assertThat(usuario2.getRole()).isEqualTo(rolReemplazo);
		verify(userRepository).saveAll(List.of(usuario1, usuario2));
	}

	@Test
	void toggleRoleActive_inactivoConUsuarios_activaSinReasignar() {
		Role role = roleConId(1L, false);
		when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
		when(userRepository.findByRole_Id(1L)).thenReturn(List.of(new User()));
		when(roleRepository.save(role)).thenReturn(role);

		RoleActiveUpdateResultDto resultado = roleService.toggleRoleActive(1L, new RoleActiveUpdateDto());

		assertThat(resultado.getActive()).isTrue();
		assertThat(resultado.getReassignedUsers()).isZero();
		verify(userRepository, never()).saveAll(any());
	}

	private Role roleConId(Long id, boolean active) {
		Role role = new Role();
		role.setId(id);
		role.setActive(active);
		return role;
	}
}
