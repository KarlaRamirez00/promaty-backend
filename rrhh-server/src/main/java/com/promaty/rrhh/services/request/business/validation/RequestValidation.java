package com.promaty.rrhh.services.request.business.validation;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.request.ContractPendingDataDto;
import com.promaty.rrhh.dto.request.CreateRequestDto;
import com.promaty.rrhh.dto.request.TerminationPendingDataDto;
import com.promaty.rrhh.dto.termination.TerminationDateRangeDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.entity.RequestAction;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.annex.business.validation.AnnexValidation;
import com.promaty.rrhh.services.contract.business.validation.ContractValidation;
import com.promaty.rrhh.services.request.handler.RequestHandlerRegistry;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;
import com.promaty.rrhh.services.shared.StaffEligibility;
import com.promaty.rrhh.services.termination.business.TerminationDateRangeCalculator;

@Component
public class RequestValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";
	private static final String CODE_CONTRACT_ACTIVE = "ACTIVE";
	private static final List<String> CODES_FINIQUITO_PENDIENTE = List.of("PENDING_APPROVAL", "PENDING_VALIDATION");

	private final StaffRepository staffRepository;
	private final ContractRepository contractRepository;
	private final LegalTerminationCauseRepository legalTerminationCauseRepository;
	private final NonRehireReasonRepository nonRehireReasonRepository;
	private final TerminationRepository terminationRepository;
	private final RequestHandlerRegistry handlerRegistry;
	private final AnnexValidation annexValidation;
	private final ContractValidation contractValidation;

	public RequestValidation(
		StaffRepository staffRepository,
		ContractRepository contractRepository,
		LegalTerminationCauseRepository legalTerminationCauseRepository,
		NonRehireReasonRepository nonRehireReasonRepository,
		TerminationRepository terminationRepository,
		RequestHandlerRegistry handlerRegistry,
		AnnexValidation annexValidation,
		ContractValidation contractValidation
	) {
		this.staffRepository = staffRepository;
		this.contractRepository = contractRepository;
		this.legalTerminationCauseRepository = legalTerminationCauseRepository;
		this.nonRehireReasonRepository = nonRehireReasonRepository;
		this.terminationRepository = terminationRepository;
		this.handlerRegistry = handlerRegistry;
		this.annexValidation = annexValidation;
		this.contractValidation = contractValidation;
	}

	public void validateCreate(CreateRequestDto dto) {
		validarPermisoDeCreacion(dto);
		Map<String, String> errores = new LinkedHashMap<>();

		if (dto.getEntityType() == RequestEntityType.CONTRACT) {
			validarProjectId(dto, errores);
			validarSolicitudDeContrato(dto, errores);
		} else if (dto.getEntityType() == RequestEntityType.TERMINATION) {
			validarSolicitudDeFiniquito(dto, errores);
		} else if (dto.getEntityType() == RequestEntityType.ANNEX) {
			validateAnnexRequest(dto, errores);
		}

		lanzarSiHayErrores(errores);
	}

	private void validateAnnexRequest(CreateRequestDto dto, Map<String, String> errores) {
		if (dto.getAction() != RequestAction.CREATE) {
			errores.put("action", "Por ahora solo se soporta crear un anexo nuevo (CREATE).");
			return;
		}
		if (dto.getAnnexData() == null) {
			errores.put("annexData", "Los datos del anexo son obligatorios para este tipo de solicitud.");
			return;
		}
		annexValidation.collectCreateErrors(dto.getAnnexData(), errores);
	}

	/**
	 * El @PreAuthorize del controller solo exige "algún" permiso de creación entre todos los tipos
	 * de Request (hasAnyAuthority) — acá se verifica el permiso específico del entityType que el
	 * cliente efectivamente está pidiendo crear, para que tener contract.create no alcance para
	 * crear un Termination (ni viceversa).
	 */
	private void validarPermisoDeCreacion(CreateRequestDto dto) {
		if (dto.getEntityType() == null) {
			return;
		}
		String permisoRequerido = handlerRegistry.get(dto.getEntityType()).createPermission();
		if (!CurrentUserAuthorities.get().contains(permisoRequerido)) {
			throw new AccessDeniedException("No tiene el permiso requerido para crear este tipo de solicitud.");
		}
	}

	private void validarProjectId(CreateRequestDto dto, Map<String, String> errores) {
		if (dto.getProjectId() == null) {
			errores.put("projectId", "El centro de costo es obligatorio.");
			return;
		}
		contractValidation.collectProjectErrors(dto.getProjectId(), errores);
	}

	private void validarSolicitudDeContrato(CreateRequestDto dto, Map<String, String> errores) {
		if (dto.getAction() != RequestAction.CREATE) {
			errores.put("action", "Por ahora solo se soporta crear un contrato nuevo (CREATE).");
			return;
		}
		ContractPendingDataDto datos = dto.getContractData();
		if (datos == null) {
			errores.put("contractData", "Los datos del contrato son obligatorios para este tipo de solicitud.");
			return;
		}

		if (datos.getStaffId() != null && !staffRepository.existsById(datos.getStaffId())) {
			errores.put("staffId", "El colaborador indicado no existe.");
		}
		contractValidation.collectFieldErrors(datos, null, errores);
		validarStaffSinContratoActivo(errores, datos);
		validateStaffHireable(errores, datos);
	}

	private void validateStaffHireable(Map<String, String> errores, ContractPendingDataDto datos) {
		if (datos.getStaffId() == null) {
			return;
		}
		staffRepository.findById(datos.getStaffId())
			.ifPresent(staff -> StaffEligibility.collectHiringErrors(staff, errores));
	}

	private void validarStaffSinContratoActivo(Map<String, String> errores, ContractPendingDataDto datos) {
		if (datos.getStaffId() != null
			&& contractRepository.findByStaffIdAndStatus_Code(datos.getStaffId(), CODE_CONTRACT_ACTIVE).isPresent()) {
			errores.put("staffId", "El colaborador ya tiene un contrato activo.");
		}
	}

	private void validarSolicitudDeFiniquito(CreateRequestDto dto, Map<String, String> errores) {
		if (dto.getAction() != RequestAction.CREATE) {
			errores.put("action", "Por ahora solo se soporta crear un finiquito nuevo (CREATE).");
			return;
		}
		TerminationPendingDataDto datos = dto.getTerminationData();
		if (datos == null) {
			errores.put("terminationData", "Los datos del finiquito son obligatorios para este tipo de solicitud.");
			return;
		}

		Contract contratoActivo = validarStaffConContratoActivo(errores, datos);
		validarCausaLegal(errores, datos);
		validarMotivoDeNoRecontratacion(errores, datos);
		if (contratoActivo != null) {
			validarFechaDeTermino(errores, datos, contratoActivo);
			validarSinFiniquitoPendiente(errores, contratoActivo);
		}
	}

	private Contract validarStaffConContratoActivo(Map<String, String> errores, TerminationPendingDataDto datos) {
		if (datos.getStaffId() == null) {
			return null;
		}
		if (!staffRepository.existsById(datos.getStaffId())) {
			errores.put("staffId", "El colaborador indicado no existe.");
			return null;
		}
		Optional<Contract> contratoActivo = contractRepository.findByStaffIdAndStatus_Code(datos.getStaffId(), CODE_CONTRACT_ACTIVE);
		if (contratoActivo.isEmpty()) {
			errores.put("staffId", "El colaborador no tiene un contrato activo.");
		}
		return contratoActivo.orElse(null);
	}

	private void validarCausaLegal(Map<String, String> errores, TerminationPendingDataDto datos) {
		if (datos.getLegalTerminationCauseId() == null) {
			return;
		}
		Optional<LegalTerminationCause> causa = legalTerminationCauseRepository.findById(datos.getLegalTerminationCauseId());
		if (causa.isEmpty()) {
			errores.put("legalTerminationCauseId", "La causal legal indicada no existe.");
		} else if (!Boolean.TRUE.equals(causa.get().getActive())) {
			errores.put("legalTerminationCauseId", "La causal legal indicada no está activa.");
		}
	}

	private void validarMotivoDeNoRecontratacion(Map<String, String> errores, TerminationPendingDataDto datos) {
		if (!Boolean.TRUE.equals(datos.getDefinitiveTermination())) {
			if (datos.getNonRehireReasonId() != null) {
				errores.put("nonRehireReasonId", "El motivo de no recontratación solo aplica si la desvinculación es definitiva.");
			}
			return;
		}
		if (datos.getNonRehireReasonId() == null) {
			errores.put("nonRehireReasonId", "El motivo de no recontratación es obligatorio si la desvinculación es definitiva.");
			return;
		}
		Optional<NonRehireReason> motivo = nonRehireReasonRepository.findById(datos.getNonRehireReasonId());
		if (motivo.isEmpty()) {
			errores.put("nonRehireReasonId", "El motivo de no recontratación indicado no existe.");
		} else if (!Boolean.TRUE.equals(motivo.get().getActive())) {
			errores.put("nonRehireReasonId", "El motivo de no recontratación indicado no está activo.");
		}
	}

	private void validarFechaDeTermino(Map<String, String> errores, TerminationPendingDataDto datos, Contract contrato) {
		LocalDate terminationDate = datos.getTerminationDate();
		if (terminationDate == null) {
			return;
		}
		TerminationDateRangeDto rango = TerminationDateRangeCalculator.calculate(contrato.getStartDate(), contrato.getEndDate());
		if (terminationDate.isBefore(rango.getMinTerminationDate())) {
			errores.put("terminationDate", "La fecha de término no puede ser anterior a " + rango.getMinTerminationDate() + ".");
			return;
		}
		if (rango.getMaxTerminationDate() != null && terminationDate.isAfter(rango.getMaxTerminationDate())) {
			errores.put("terminationDate", "La fecha de término no puede ser posterior al término del contrato.");
		}
	}

	private void validarSinFiniquitoPendiente(Map<String, String> errores, Contract contrato) {
		if (!terminationRepository.findByContract_IdAndStatus_CodeIn(contrato.getId(), CODES_FINIQUITO_PENDIENTE).isEmpty()) {
			errores.put("staffId", "Ya existe un finiquito pendiente para el contrato activo de este colaborador.");
		}
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
