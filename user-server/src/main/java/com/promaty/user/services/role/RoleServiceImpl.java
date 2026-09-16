package com.promaty.user.services.role;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.user.dto.role.CreateRoleDto;
import com.promaty.user.dto.role.RoleDetailDto;
import com.promaty.user.dto.role.RoleFilterParams;
import com.promaty.user.dto.role.RoleListDto;
import com.promaty.user.dto.role.RoleActiveUpdateDto;
import com.promaty.user.dto.role.RoleActiveUpdateResultDto;
import com.promaty.user.dto.role.UpdateRoleDto;
import com.promaty.user.dto.shared.Action;
import com.promaty.user.entity.Permission;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.SubModule;
import com.promaty.user.entity.User;
import com.promaty.user.exception.BusinessValidationException;
import com.promaty.user.exception.ResourceNotFoundException;
import com.promaty.user.repository.RoleRepository;
import com.promaty.user.repository.UserRepository;
import com.promaty.user.repository.projection.RoleUserCount;
import com.promaty.user.services.role.business.builder.CreateRoleBuilder;
import com.promaty.user.services.role.business.builder.RoleQueryBuilder;
import com.promaty.user.services.role.business.builder.RoleRelationsResolver;
import com.promaty.user.services.role.business.builder.UpdateRoleBuilder;
import com.promaty.user.services.role.business.mapper.RoleMapper;
import com.promaty.user.services.role.business.validation.RoleValidation;
import com.promaty.user.services.shared.ActionsResolver;
import com.promaty.user.services.shared.CurrentUserAuthorities;

@Service
public class RoleServiceImpl implements RoleService {

	private static final String ROL_NO_ENCONTRADO = "Rol no encontrado.";
	private static final String ROL_REEMPLAZO_NO_ENCONTRADO = "El rol de reemplazo no existe.";
	private static final String ROL_DE_SISTEMA = "Este rol es del sistema y no puede editarse ni desactivarse.";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"role.update", Action.UPDATE,
		"role.active", Action.ACTIVE
	);

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final RoleValidation roleValidation;
	private final CreateRoleBuilder createRoleBuilder;
	private final RoleRelationsResolver relationsResolver;
	private final ActionsResolver actionsResolver;

	public RoleServiceImpl(
		RoleRepository roleRepository,
		UserRepository userRepository,
		RoleValidation roleValidation,
		CreateRoleBuilder createRoleBuilder,
		RoleRelationsResolver relationsResolver,
		ActionsResolver actionsResolver
	) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.roleValidation = roleValidation;
		this.createRoleBuilder = createRoleBuilder;
		this.relationsResolver = relationsResolver;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createRole(CreateRoleDto dto) {
		roleValidation.validateCreate(dto);
		Role role = createRoleBuilder.build(dto);
		return roleRepository.save(role).getId();
	}

	@Override
	@Transactional
	public void updateRole(Long id, UpdateRoleDto dto) {
		Role existente = buscarPorId(id);
		rechazarSiEsDeSistema(existente);
		roleValidation.validateUpdate(id, dto);
		Set<Permission> permisos = relationsResolver.resolvePermissions(dto.getPermissionIds());
		Set<SubModule> subModulos = relationsResolver.resolveSubModules(dto.getSubModuleIds());
		UpdateRoleBuilder.apply(existente, dto, permisos, subModulos);
		roleRepository.save(existente);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RoleListDto> listRoles(RoleFilterParams filters, Pageable pageable) {
		Specification<Role> especificacion = RoleQueryBuilder.fromFilters(filters);
		Page<Role> roles = roleRepository.findAll(especificacion, pageable);

		Map<Long, Long> usuariosPorRol = userRepository.countUsersGroupedByRole().stream()
			.collect(Collectors.toMap(RoleUserCount::getRoleId, RoleUserCount::getTotal));

		return roles.map(role -> {
			RoleListDto dto = RoleMapper.toListDto(role, usuariosPorRol.getOrDefault(role.getId(), 0L));
			dto.setActions(resolveActions(role));
			return dto;
		});
	}

	@Override
	@Transactional(readOnly = true)
	public RoleDetailDto getRoleDetail(Long id) {
		Role role = buscarPorId(id);
		long totalUsers = userRepository.countByRole_Id(id);
		RoleDetailDto detalle = RoleMapper.toDetailDto(role, totalUsers);
		detalle.setActions(resolveActions(role));
		return detalle;
	}

	@Override
	@Transactional
	public RoleActiveUpdateResultDto toggleRoleActive(Long id, RoleActiveUpdateDto dto) {
		Role role = buscarPorId(id);
		rechazarSiEsDeSistema(role);
		List<User> usuariosAsignados = userRepository.findByRole_Id(id);
		long reasignados = 0;

		boolean seEstaDesactivando = role.getActive();
		if (seEstaDesactivando && !usuariosAsignados.isEmpty()) {
			Role nuevoRol = resolverRolDeReemplazo(dto.getNewRoleId(), usuariosAsignados.size());
			usuariosAsignados.forEach(usuario -> usuario.setRole(nuevoRol));
			userRepository.saveAll(usuariosAsignados);
			reasignados = usuariosAsignados.size();
		}

		role.toggleActive();
		roleRepository.save(role);
		return new RoleActiveUpdateResultDto(role.getId(), role.getActive(), reasignados);
	}

	private Role resolverRolDeReemplazo(Long newRoleId, int totalUsuarios) {
		if (newRoleId == null) {
			throw new BusinessValidationException(
				"Este rol tiene usuarios asignados, se requiere un rol de reemplazo.",
				Map.of("newRoleId", "Este rol tiene " + totalUsuarios + " usuario(s) asignado(s). Indica un rol de reemplazo.")
			);
		}
		return roleRepository.findById(newRoleId)
			.orElseThrow(() -> new ResourceNotFoundException(ROL_REEMPLAZO_NO_ENCONTRADO));
	}

	private Role buscarPorId(Long id) {
		return roleRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(ROL_NO_ENCONTRADO));
	}

	private void rechazarSiEsDeSistema(Role role) {
		if (Boolean.TRUE.equals(role.getSystem())) {
			throw new BusinessValidationException(ROL_DE_SISTEMA, Map.of("id", ROL_DE_SISTEMA));
		}
	}

	private List<Action> resolveActions(Role role) {
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		if (!Boolean.TRUE.equals(role.getSystem())) {
			return actions;
		}
		return actions.stream()
			.filter(action -> action != Action.UPDATE && action != Action.ACTIVE)
			.collect(Collectors.toList());
	}
}
