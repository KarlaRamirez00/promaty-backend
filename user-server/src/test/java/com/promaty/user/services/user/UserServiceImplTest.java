package com.promaty.user.services.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.promaty.user.dto.shared.Action;
import com.promaty.user.dto.user.UserDetailDto;
import com.promaty.user.dto.user.UserFilterParams;
import com.promaty.user.dto.user.UserListDto;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.exception.ResourceNotFoundException;
import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.UserProjectAccessRepository;
import com.promaty.user.repository.UserRepository;
import com.promaty.user.services.shared.ActionsResolver;
import com.promaty.user.services.user.business.builder.CreateUserBuilder;
import com.promaty.user.services.user.business.validation.UserValidation;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private UserProjectAccessRepository userProjectAccessRepository;

	@Mock
	private UserValidation userValidation;

	@Mock
	private CreateUserBuilder createUserBuilder;

	@Mock
	private ActionsResolver actionsResolver;

	@InjectMocks
	private UserServiceImpl userService;

	@Test
	void getUserDetail_usuarioNoExiste_lanzaResourceNotFoundException() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getUserDetail(1L))
			.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getUserDetail_conPermisoDeActualizarYActivar_actionsIncluyeAmbos() {
		User user = usuarioConId(1L);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userProjectAccessRepository.findProjectIdsByUserId(1L)).thenReturn(List.of());
		when(actionsResolver.resolve(any(), any())).thenReturn(List.of(Action.UPDATE, Action.ACTIVE));

		UserDetailDto detalle = userService.getUserDetail(1L);

		assertThat(detalle.getActions()).containsExactly(Action.UPDATE, Action.ACTIVE);
	}

	@Test
	void listUsers_sinPermisosDeDecision_actionsQuedaVacio() {
		User user = usuarioConId(2L);
		Pageable pageable = PageRequest.of(0, 20);
		when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(user)));
		when(actionsResolver.resolve(any(), any())).thenReturn(List.of());

		List<UserListDto> resultado = userService.listUsers(new UserFilterParams(), pageable).getContent();

		assertThat(resultado.get(0).getActions()).isEmpty();
	}

	@Test
	void toggleUserActive_usuarioExiste_invierteActiveYResuelveActions() {
		User user = usuarioConId(3L);
		user.setActive(true);
		when(userRepository.findById(3L)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userProjectAccessRepository.findProjectIdsByUserId(3L)).thenReturn(List.of());
		when(actionsResolver.resolve(any(), any())).thenReturn(List.of(Action.ACTIVE));

		UserDetailDto detalle = userService.toggleUserActive(3L);

		assertThat(user.getActive()).isFalse();
		assertThat(detalle.getActions()).containsExactly(Action.ACTIVE);
	}

	private User usuarioConId(Long id) {
		User user = new User();
		user.setId(id);
		user.setFirstName("Ana");
		user.setLastName("Perez");
		user.setEmail("ana.perez@promaty.cl");
		user.setActive(true);
		Role role = new Role();
		role.setId(1L);
		role.setName("Editor");
		user.setRole(role);
		return user;
	}
}
