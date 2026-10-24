package com.promaty.rrhh.services.termination.business.validation;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.termination.UpdateTerminationDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.LegalTerminationCause;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;

@Component
public class TerminationValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";
	private static final List<String> CODES_FINIQUITO_EDITABLE = List.of("PENDING_APPROVAL", "PENDING_VALIDATION");
	private static final int DIAS_MAXIMO_RETROACTIVO = 15;

	private final LegalTerminationCauseRepository legalTerminationCauseRepository;
	private final NonRehireReasonRepository nonRehireReasonRepository;

	public TerminationValidation(
		LegalTerminationCauseRepository legalTerminationCauseRepository,
		NonRehireReasonRepository nonRehireReasonRepository
	) {
		this.legalTerminationCauseRepository = legalTerminationCauseRepository;
		this.nonRehireReasonRepository = nonRehireReasonRepository;
	}

	public void validateUpdate(Termination existente, UpdateTerminationDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (!CODES_FINIQUITO_EDITABLE.contains(existente.getStatus().getCode())) {
			errores.put("status", "Este finiquito ya fue decidido, no admite edición.");
			lanzarSiHayErrores(errores);
			return;
		}

		validarCausaLegal(errores, dto.getLegalTerminationCauseId());
		validarMotivoDeNoRecontratacion(errores, dto);
		validarFechaDeTermino(errores, dto.getTerminationDate(), existente.getContract());

		lanzarSiHayErrores(errores);
	}

	private void validarCausaLegal(Map<String, String> errores, Long legalTerminationCauseId) {
		if (legalTerminationCauseId == null) {
			return;
		}
		Optional<LegalTerminationCause> causa = legalTerminationCauseRepository.findById(legalTerminationCauseId);
		if (causa.isEmpty()) {
			errores.put("legalTerminationCauseId", "La causal legal indicada no existe.");
		} else if (!Boolean.TRUE.equals(causa.get().getActive())) {
			errores.put("legalTerminationCauseId", "La causal legal indicada no está activa.");
		}
	}

	private void validarMotivoDeNoRecontratacion(Map<String, String> errores, UpdateTerminationDto dto) {
		if (!Boolean.TRUE.equals(dto.getDefinitiveTermination())) {
			if (dto.getNonRehireReasonId() != null) {
				errores.put("nonRehireReasonId", "El motivo de no recontratación solo aplica si la desvinculación es definitiva.");
			}
			return;
		}
		if (dto.getNonRehireReasonId() == null) {
			errores.put("nonRehireReasonId", "El motivo de no recontratación es obligatorio si la desvinculación es definitiva.");
			return;
		}
		Optional<NonRehireReason> motivo = nonRehireReasonRepository.findById(dto.getNonRehireReasonId());
		if (motivo.isEmpty()) {
			errores.put("nonRehireReasonId", "El motivo de no recontratación indicado no existe.");
		} else if (!Boolean.TRUE.equals(motivo.get().getActive())) {
			errores.put("nonRehireReasonId", "El motivo de no recontratación indicado no está activo.");
		}
	}

	private void validarFechaDeTermino(Map<String, String> errores, LocalDate terminationDate, Contract contrato) {
		if (terminationDate == null) {
			return;
		}
		LocalDate minimoRetroactivo = LocalDate.now().minusDays(DIAS_MAXIMO_RETROACTIVO);
		if (terminationDate.isBefore(minimoRetroactivo)) {
			errores.put("terminationDate", "La fecha de término no puede ser anterior a " + DIAS_MAXIMO_RETROACTIVO + " días atrás.");
			return;
		}
		if (terminationDate.isBefore(contrato.getStartDate())) {
			errores.put("terminationDate", "La fecha de término no puede ser anterior al inicio del contrato.");
			return;
		}
		if (contrato.getEndDate() != null && terminationDate.isAfter(contrato.getEndDate())) {
			errores.put("terminationDate", "La fecha de término no puede ser posterior al término del contrato.");
		}
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
