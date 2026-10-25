package com.promaty.rrhh.services.request.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.rrhh.dto.request.TerminationPendingDataDto;
import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.LegalTerminationCauseRepository;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.rexplus.RexPlusClient;
import com.promaty.rrhh.services.rexplus.RexSyncResult;

/**
 * RequestValidation ya confirmó que el colaborador tiene contrato activo y que las FK existen al
 * crear la Request; para cuando este Handler corre (después de días, por la aprobación en dos
 * niveles) pueden haber cambiado, de ahí el orElseThrow defensivo en cada resolución — mismo patrón
 * que ContractRequestHandler.
 */
@Component
public class TerminationRequestHandler implements RequestHandler {

	private static final String SUBMODULE_TERMINATION = "termination";
	private static final String SUBMODULE_CONTRACT = "contract";
	private static final String SUBMODULE_STAFF = "staff";
	private static final String CODE_PENDING_APPROVAL = "PENDING_APPROVAL";
	private static final String CODE_PENDING_VALIDATION = "PENDING_VALIDATION";
	private static final String CODE_APPROVED = "APPROVED";
	private static final String CODE_REJECTED = "REJECTED";
	private static final String CODE_SYNC_ERROR = "SYNC_ERROR";
	private static final String CODE_CONTRACT_ACTIVE = "ACTIVE";
	private static final String CODE_CONTRACT_TERMINATED = "TERMINATED";
	private static final String CODE_STAFF_BLOCKED = "BLOCKED";
	private static final String CODE_STAFF_UNLINKED = "UNLINKED";
	private static final String PERMISO_APROBAR = "termination.approve";
	private static final String PERMISO_VALIDAR = "termination.validate";
	private static final String PERMISO_CREAR = "termination.create";
	private static final String PERMISO_LEER = "termination.read";

	private final ObjectMapper objectMapper;
	private final StaffRepository staffRepository;
	private final ContractRepository contractRepository;
	private final LegalTerminationCauseRepository legalTerminationCauseRepository;
	private final NonRehireReasonRepository nonRehireReasonRepository;
	private final TerminationRepository terminationRepository;
	private final PlatformStatusRepository platformStatusRepository;
	private final RexPlusClient rexPlusClient;

	public TerminationRequestHandler(
		ObjectMapper objectMapper,
		StaffRepository staffRepository,
		ContractRepository contractRepository,
		LegalTerminationCauseRepository legalTerminationCauseRepository,
		NonRehireReasonRepository nonRehireReasonRepository,
		TerminationRepository terminationRepository,
		PlatformStatusRepository platformStatusRepository,
		RexPlusClient rexPlusClient
	) {
		this.objectMapper = objectMapper;
		this.staffRepository = staffRepository;
		this.contractRepository = contractRepository;
		this.legalTerminationCauseRepository = legalTerminationCauseRepository;
		this.nonRehireReasonRepository = nonRehireReasonRepository;
		this.terminationRepository = terminationRepository;
		this.platformStatusRepository = platformStatusRepository;
		this.rexPlusClient = rexPlusClient;
	}

	@Override
	public RequestEntityType supports() {
		return RequestEntityType.TERMINATION;
	}

	@Override
	public String approvePermission() {
		return PERMISO_APROBAR;
	}

	@Override
	public String validatePermission() {
		return PERMISO_VALIDAR;
	}

	@Override
	public String createPermission() {
		return PERMISO_CREAR;
	}

	@Override
	public String readPermission() {
		return PERMISO_LEER;
	}

	@Override
	public Long create(Request request) {
		TerminationPendingDataDto datos = deserializar(request.getPendingData());

		Termination termination = new Termination();
		termination.setStaff(staffRepository.findById(datos.getStaffId())
			.orElseThrow(() -> new ResourceNotFoundException("El colaborador indicado no existe.")));
		termination.setContract(contractRepository.findByStaffIdAndStatus_Code(datos.getStaffId(), CODE_CONTRACT_ACTIVE)
			.orElseThrow(() -> new ResourceNotFoundException("El colaborador no tiene un contrato activo.")));
		termination.setProject(request.getProject());
		termination.setTerminationDate(datos.getTerminationDate());
		termination.setLegalTerminationCause(legalTerminationCauseRepository.findById(datos.getLegalTerminationCauseId())
			.orElseThrow(() -> new ResourceNotFoundException("La causal legal indicada no existe.")));
		termination.setDefinitiveTermination(datos.getDefinitiveTermination());
		if (datos.getNonRehireReasonId() != null) {
			termination.setNonRehireReason(nonRehireReasonRepository.findById(datos.getNonRehireReasonId())
				.orElseThrow(() -> new ResourceNotFoundException("El motivo de no recontratación indicado no existe.")));
		}
		termination.setNotes(datos.getNotes());
		termination.setStatus(resolveEstadoTermination(CODE_PENDING_APPROVAL));
		return terminationRepository.save(termination).getId();
	}

	@Override
	public void applyDecision(Request request, ApprovalLevel level, ApprovalDecision decision) {
		Termination termination = terminationRepository.findById(request.getEntityId())
			.orElseThrow(() -> new ResourceNotFoundException("El finiquito de la solicitud no existe."));

		if (decision == ApprovalDecision.REJECTED) {
			termination.setStatus(resolveEstadoTermination(CODE_REJECTED));
			terminationRepository.save(termination);
			return;
		}
		if (level == ApprovalLevel.PROJECT_MANAGER) {
			termination.setStatus(resolveEstadoTermination(CODE_PENDING_VALIDATION));
			terminationRepository.save(termination);
			return;
		}
		aplicarValidacionRrhh(termination);
	}

	private void aplicarValidacionRrhh(Termination termination) {
		if (CODE_APPROVED.equals(termination.getStatus().getCode())) {
			return;
		}

		RexSyncResult sincronizacion = rexPlusClient.syncTermination(termination);
		if (!sincronizacion.success()) {
			termination.setStatus(resolveEstadoTermination(CODE_SYNC_ERROR));
			terminationRepository.save(termination);
			return;
		}

		Contract contrato = termination.getContract();
		termination.setContractEndDateBeforeTermination(contrato.getEndDate());
		contrato.setEndDate(termination.getTerminationDate());
		contrato.setStatus(resolveEstadoContrato(CODE_CONTRACT_TERMINATED));
		contractRepository.save(contrato);

		Staff staff = termination.getStaff();
		staff.setStatus(resolveEstadoStaff(Boolean.TRUE.equals(termination.getDefinitiveTermination())
			? CODE_STAFF_BLOCKED
			: CODE_STAFF_UNLINKED));
		staffRepository.save(staff);

		termination.setRexTerminationNumber(sincronizacion.externalReferenceNumber());
		termination.setStatus(resolveEstadoTermination(CODE_APPROVED));
		terminationRepository.save(termination);
	}

	private PlatformStatus resolveEstadoTermination(String code) {
		return platformStatusRepository.findBySubModuleAndCode(SUBMODULE_TERMINATION, code)
			.orElseThrow(() -> new ResourceNotFoundException("El estado " + code + " de finiquito no está sembrado."));
	}

	private PlatformStatus resolveEstadoContrato(String code) {
		return platformStatusRepository.findBySubModuleAndCode(SUBMODULE_CONTRACT, code)
			.orElseThrow(() -> new ResourceNotFoundException("El estado " + code + " de contrato no está sembrado."));
	}

	private PlatformStatus resolveEstadoStaff(String code) {
		return platformStatusRepository.findBySubModuleAndCode(SUBMODULE_STAFF, code)
			.orElseThrow(() -> new ResourceNotFoundException("El estado " + code + " de colaborador no está sembrado."));
	}

	private TerminationPendingDataDto deserializar(String pendingData) {
		try {
			return objectMapper.readValue(pendingData, TerminationPendingDataDto.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("El pendingData de la solicitud es invalido.", e);
		}
	}
}
