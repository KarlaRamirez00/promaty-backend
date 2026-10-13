package com.promaty.user.services.user;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.user.dto.shared.Action;
import com.promaty.user.dto.user.CreateUserDto;
import com.promaty.user.dto.user.UpdateUserDto;
import com.promaty.user.dto.user.UserDetailDto;
import com.promaty.user.dto.user.UserFilterParams;
import com.promaty.user.dto.user.UserListDto;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.entity.UserProjectAccess;
import com.promaty.user.exception.ResourceNotFoundException;
import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.UserProjectAccessRepository;
import com.promaty.user.repository.UserRepository;
import com.promaty.user.services.shared.ActionsResolver;
import com.promaty.user.services.shared.CurrentUserAuthorities;
import com.promaty.user.services.user.business.builder.CreateUserBuilder;
import com.promaty.user.services.user.business.builder.UpdateUserBuilder;
import com.promaty.user.services.user.business.builder.UserQueryBuilder;
import com.promaty.user.services.user.business.mapper.UserMapper;
import com.promaty.user.services.user.business.validation.UserValidation;

@Service
public class UserServiceImpl implements UserService {

	private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado.";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"user.update", Action.UPDATE,
		"user.active", Action.ACTIVE
	);

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserProjectAccessRepository userProjectAccessRepository;
	private final UserValidation userValidation;
	private final CreateUserBuilder createUserBuilder;
	private final ActionsResolver actionsResolver;

	public UserServiceImpl(
		UserRepository userRepository,
		RoleRepository roleRepository,
		UserProjectAccessRepository userProjectAccessRepository,
		UserValidation userValidation,
		CreateUserBuilder createUserBuilder,
		ActionsResolver actionsResolver
	) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.userProjectAccessRepository = userProjectAccessRepository;
		this.userValidation = userValidation;
		this.createUserBuilder = createUserBuilder;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createUser(CreateUserDto dto) {
		userValidation.validateCreate(dto);
		User user = createUserBuilder.build(dto);
		User guardado = userRepository.save(user);
		guardarAccesoAProyectos(guardado, dto.getProjectIds());
		return guardado.getId();
	}

	@Override
	@Transactional
	public void updateUser(Long id, UpdateUserDto dto) {
		userValidation.validateUpdate(id, dto);
		User existente = buscarPorId(id);
		Role role = roleRepository.getReferenceById(dto.getRoleId());
		UpdateUserBuilder.apply(existente, dto, role);
		userRepository.save(existente);
		guardarAccesoAProyectos(existente, dto.getProjectIds());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<UserListDto> listUsers(UserFilterParams filters, Pageable pageable) {
		Specification<User> especificacion = UserQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		return userRepository.findAll(especificacion, pageable)
			.map(UserMapper::toListDto)
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetailDto getUserDetail(Long id) {
		User user = buscarPorId(id);
		return conAcciones(UserMapper.toDetailDto(user, userProjectAccessRepository.findProjectIdsByUserId(id)));
	}

	@Override
	@Transactional
	public UserDetailDto toggleUserActive(Long id) {
		User user = buscarPorId(id);
		user.toggleActive();
		User guardado = userRepository.save(user);
		return conAcciones(UserMapper.toDetailDto(guardado, userProjectAccessRepository.findProjectIdsByUserId(id)));
	}

	private void guardarAccesoAProyectos(User user, List<Long> projectIds) {
		userProjectAccessRepository.deleteByUser_Id(user.getId());
		userProjectAccessRepository.flush();
		if (projectIds == null || projectIds.isEmpty()) {
			return;
		}
		List<UserProjectAccess> accesos = projectIds.stream()
			.distinct()
			.map(projectId -> {
				UserProjectAccess acceso = new UserProjectAccess();
				acceso.setUser(user);
				acceso.setProjectId(projectId);
				return acceso;
			})
			.toList();
		userProjectAccessRepository.saveAll(accesos);
	}

	private User buscarPorId(Long id) {
		return userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(USUARIO_NO_ENCONTRADO));
	}

	private UserListDto conAcciones(UserListDto dto, List<Action> actions) {
		dto.setActions(actions);
		return dto;
	}

	private UserDetailDto conAcciones(UserDetailDto dto) {
		dto.setActions(actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES));
		return dto;
	}
}
