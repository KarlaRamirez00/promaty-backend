package com.promaty.rrhh.services.requestrejectionreason;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.requestrejectionreason.CreateRequestRejectionReasonDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonDetailDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonFilterParams;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonListDto;
import com.promaty.rrhh.dto.requestrejectionreason.RequestRejectionReasonOptionDto;
import com.promaty.rrhh.dto.requestrejectionreason.UpdateRequestRejectionReasonDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.RequestRejectionReason;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.RequestRejectionReasonRepository;
import com.promaty.rrhh.services.requestrejectionreason.business.builder.RequestRejectionReasonQueryBuilder;
import com.promaty.rrhh.services.requestrejectionreason.business.mapper.RequestRejectionReasonMapper;
import com.promaty.rrhh.services.requestrejectionreason.business.validation.RequestRejectionReasonValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;

@Service
public class RequestRejectionReasonServiceImpl implements RequestRejectionReasonService {

	private static final String NO_ENCONTRADO = "Motivo de rechazo no encontrado.";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"requestRejectionReason.update", Action.UPDATE,
		"requestRejectionReason.active", Action.ACTIVE
	);

	private final RequestRejectionReasonRepository requestRejectionReasonRepository;
	private final RequestRejectionReasonValidation requestRejectionReasonValidation;
	private final ActionsResolver actionsResolver;

	public RequestRejectionReasonServiceImpl(
			RequestRejectionReasonRepository requestRejectionReasonRepository,
			RequestRejectionReasonValidation requestRejectionReasonValidation,
			ActionsResolver actionsResolver) {
		this.requestRejectionReasonRepository = requestRejectionReasonRepository;
		this.requestRejectionReasonValidation = requestRejectionReasonValidation;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createRequestRejectionReason(CreateRequestRejectionReasonDto dto) {
		requestRejectionReasonValidation.validateCreate(dto);
		RequestRejectionReason reason = new RequestRejectionReason();
		reason.setName(dto.getName());
		reason.setDescription(dto.getDescription());
		reason.setSubModule(dto.getSubModule());
		return requestRejectionReasonRepository.save(reason).getId();
	}

	@Override
	@Transactional
	public void updateRequestRejectionReason(Long id, UpdateRequestRejectionReasonDto dto) {
		requestRejectionReasonValidation.validateUpdate(id, dto);
		RequestRejectionReason existente = buscarPorId(id);
		existente.setName(dto.getName());
		existente.setDescription(dto.getDescription());
		existente.setSubModule(dto.getSubModule());
		requestRejectionReasonRepository.save(existente);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<RequestRejectionReasonListDto> listRequestRejectionReasons(RequestRejectionReasonFilterParams filters, Pageable pageable) {
		Specification<RequestRejectionReason> especificacion = RequestRejectionReasonQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		return requestRejectionReasonRepository.findAll(especificacion, pageable)
			.map(RequestRejectionReasonMapper::toListDto)
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public RequestRejectionReasonDetailDto getRequestRejectionReasonDetail(Long id) {
		return conAcciones(RequestRejectionReasonMapper.toDetailDto(buscarPorId(id)));
	}

	@Override
	@Transactional
	public RequestRejectionReasonDetailDto toggleRequestRejectionReasonActive(Long id) {
		RequestRejectionReason reason = buscarPorId(id);
		reason.toggleActive();
		return conAcciones(RequestRejectionReasonMapper.toDetailDto(requestRejectionReasonRepository.save(reason)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<RequestRejectionReasonOptionDto> listOptionsBySubModule(String subModule) {
		return requestRejectionReasonRepository.findBySubModuleAndActiveTrueOrderByName(subModule)
			.stream()
			.map(RequestRejectionReasonMapper::toOptionDto)
			.toList();
	}

	private RequestRejectionReason buscarPorId(Long id) {
		return requestRejectionReasonRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private RequestRejectionReasonListDto conAcciones(RequestRejectionReasonListDto dto, List<Action> actions) {
		dto.setActions(actions);
		return dto;
	}

	private RequestRejectionReasonDetailDto conAcciones(RequestRejectionReasonDetailDto dto) {
		dto.setActions(actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES));
		return dto;
	}
}
