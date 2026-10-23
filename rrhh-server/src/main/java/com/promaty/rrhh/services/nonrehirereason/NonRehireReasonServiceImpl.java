package com.promaty.rrhh.services.nonrehirereason;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.nonrehirereason.CreateNonRehireReasonDto;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonDetailDto;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonFilterParams;
import com.promaty.rrhh.dto.nonrehirereason.NonRehireReasonListDto;
import com.promaty.rrhh.dto.nonrehirereason.UpdateNonRehireReasonDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.services.nonrehirereason.business.builder.NonRehireReasonQueryBuilder;
import com.promaty.rrhh.services.nonrehirereason.business.mapper.NonRehireReasonMapper;
import com.promaty.rrhh.services.nonrehirereason.business.validation.NonRehireReasonValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;

@Service
public class NonRehireReasonServiceImpl implements NonRehireReasonService {

	private static final String NO_ENCONTRADO = "Motivo de no recontratación no encontrado.";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"nonRehireReason.update", Action.UPDATE,
		"nonRehireReason.active", Action.ACTIVE
	);

	private final NonRehireReasonRepository nonRehireReasonRepository;
	private final NonRehireReasonValidation nonRehireReasonValidation;
	private final ActionsResolver actionsResolver;

	public NonRehireReasonServiceImpl(
		NonRehireReasonRepository nonRehireReasonRepository,
		NonRehireReasonValidation nonRehireReasonValidation,
		ActionsResolver actionsResolver
	) {
		this.nonRehireReasonRepository = nonRehireReasonRepository;
		this.nonRehireReasonValidation = nonRehireReasonValidation;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createNonRehireReason(CreateNonRehireReasonDto dto) {
		nonRehireReasonValidation.validateCreate(dto);
		NonRehireReason reason = new NonRehireReason();
		reason.setName(dto.getName());
		reason.setCode(dto.getCode());
		reason.setDescription(dto.getDescription());
		return nonRehireReasonRepository.save(reason).getId();
	}

	@Override
	@Transactional
	public void updateNonRehireReason(Long id, UpdateNonRehireReasonDto dto) {
		nonRehireReasonValidation.validateUpdate(id, dto);
		NonRehireReason existente = buscarPorId(id);
		existente.setName(dto.getName());
		existente.setCode(dto.getCode());
		existente.setDescription(dto.getDescription());
		nonRehireReasonRepository.save(existente);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<NonRehireReasonListDto> listNonRehireReasons(NonRehireReasonFilterParams filters, Pageable pageable) {
		Specification<NonRehireReason> especificacion = NonRehireReasonQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		return nonRehireReasonRepository.findAll(especificacion, pageable)
			.map(NonRehireReasonMapper::toListDto)
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public NonRehireReasonDetailDto getNonRehireReasonDetail(Long id) {
		return conAcciones(NonRehireReasonMapper.toDetailDto(buscarPorId(id)));
	}

	@Override
	@Transactional
	public NonRehireReasonDetailDto toggleNonRehireReasonActive(Long id) {
		NonRehireReason reason = buscarPorId(id);
		reason.toggleActive();
		return conAcciones(NonRehireReasonMapper.toDetailDto(nonRehireReasonRepository.save(reason)));
	}

	private NonRehireReason buscarPorId(Long id) {
		return nonRehireReasonRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private NonRehireReasonListDto conAcciones(NonRehireReasonListDto dto, List<Action> actions) {
		dto.setActions(actions);
		return dto;
	}

	private NonRehireReasonDetailDto conAcciones(NonRehireReasonDetailDto dto) {
		dto.setActions(actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES));
		return dto;
	}
}
