package com.promaty.rrhh.services.termination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.dto.termination.TerminationDetailDto;
import com.promaty.rrhh.dto.termination.TerminationFilterParams;
import com.promaty.rrhh.dto.termination.TerminationListDto;
import com.promaty.rrhh.dto.termination.UpdateTerminationDto;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.ProjectAccessSpecification;
import com.promaty.rrhh.services.shared.RequestApprovalActionsResolver;
import com.promaty.rrhh.services.termination.business.builder.TerminationQueryBuilder;
import com.promaty.rrhh.services.termination.business.mapper.TerminationMapper;
import com.promaty.rrhh.services.termination.business.validation.TerminationValidation;

@Service
public class TerminationServiceImpl implements TerminationService {

	private static final String NO_ENCONTRADO = "Finiquito no encontrado.";
	private static final String PERMISO_APROBAR = "termination.approve";
	private static final String PERMISO_VALIDAR = "termination.validate";
	private static final String PERMISO_ACTUALIZAR = "termination.update";
	private static final List<String> CODES_PENDIENTE = List.of("PENDING_APPROVAL", "PENDING_VALIDATION");

	private final TerminationRepository terminationRepository;
	private final RequestRepository requestRepository;
	private final LegalTerminationCauseRepository legalTerminationCauseRepository;
	private final NonRehireReasonRepository nonRehireReasonRepository;
	private final TerminationValidation terminationValidation;
	private final RequestApprovalActionsResolver actionsResolver;

	public TerminationServiceImpl(
		TerminationRepository terminationRepository,
		RequestRepository requestRepository,
		LegalTerminationCauseRepository legalTerminationCauseRepository,
		NonRehireReasonRepository nonRehireReasonRepository,
		TerminationValidation terminationValidation,
		RequestApprovalActionsResolver actionsResolver
	) {
		this.terminationRepository = terminationRepository;
		this.requestRepository = requestRepository;
		this.legalTerminationCauseRepository = legalTerminationCauseRepository;
		this.nonRehireReasonRepository = nonRehireReasonRepository;
		this.terminationValidation = terminationValidation;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TerminationListDto> listTerminations(TerminationFilterParams filters, Pageable pageable) {
		Specification<Termination> especificacion = TerminationQueryBuilder.fromFilters(filters)
			.and(ProjectAccessSpecification.onProject());
		Page<TerminationListDto> pagina = terminationRepository.findAll(especificacion, pageable).map(TerminationMapper::toListDto);

		Map<Long, Request> requestsPorFiniquito = buscarRequestsPorFiniquito(pagina.map(TerminationListDto::getId).getContent());
		return pagina.map(dto -> conRequestYAcciones(dto, requestsPorFiniquito.get(dto.getId())));
	}

	@Override
	@Transactional(readOnly = true)
	public TerminationDetailDto getTerminationDetail(Long id) {
		Termination termination = buscarPorId(id);
		TerminationDetailDto dto = TerminationMapper.toDetailDto(termination);
		Request request = requestRepository.findByEntityTypeAndEntityId(RequestEntityType.TERMINATION, id).orElse(null);
		dto.setRequestId(request == null ? null : request.getId());
		dto.setActions(resolverAcciones(request));
		return dto;
	}

	@Override
	@Transactional
	public void updateTermination(Long id, UpdateTerminationDto dto) {
		Termination existente = buscarPorId(id);
		terminationValidation.validateUpdate(existente, dto);

		existente.setTerminationDate(dto.getTerminationDate());
		existente.setLegalTerminationCause(resolveCausaLegal(dto.getLegalTerminationCauseId()));
		existente.setDefinitiveTermination(dto.getDefinitiveTermination());
		existente.setNonRehireReason(dto.getNonRehireReasonId() == null ? null : resolveMotivoDeNoRecontratacion(dto.getNonRehireReasonId()));
		existente.setNotes(dto.getNotes());
		terminationRepository.save(existente);
	}

	private LegalTerminationCause resolveCausaLegal(Long id) {
		return legalTerminationCauseRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("La causal legal indicada no existe."));
	}

	private NonRehireReason resolveMotivoDeNoRecontratacion(Long id) {
		return nonRehireReasonRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("El motivo de no recontratación indicado no existe."));
	}

	private Termination buscarPorId(Long id) {
		return terminationRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private Map<Long, Request> buscarRequestsPorFiniquito(List<Long> terminationIds) {
		if (terminationIds.isEmpty()) {
			return Map.of();
		}
		return requestRepository.findByEntityTypeAndEntityIdIn(RequestEntityType.TERMINATION, terminationIds).stream()
			.collect(Collectors.toMap(Request::getEntityId, Function.identity()));
	}

	private TerminationListDto conRequestYAcciones(TerminationListDto dto, Request request) {
		dto.setRequestId(request == null ? null : request.getId());
		dto.setActions(resolverAcciones(request));
		return dto;
	}

	private List<Action> resolverAcciones(Request request) {
		if (request == null) {
			return List.of();
		}
		String statusCode = request.getStatus().getCode();
		List<Action> acciones = actionsResolver.resolve(statusCode, CurrentUserAuthorities.get(), PERMISO_APROBAR, PERMISO_VALIDAR);
		if (CODES_PENDIENTE.contains(statusCode) && CurrentUserAuthorities.get().contains(PERMISO_ACTUALIZAR)) {
			acciones = new ArrayList<>(acciones);
			acciones.add(Action.UPDATE);
		}
		return acciones;
	}
}
