package com.promaty.rrhh.services.request.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promaty.rrhh.dto.request.AnnexPendingDataDto;
import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.ApprovalDecision;
import com.promaty.rrhh.entity.ApprovalLevel;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.AnnexRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.services.annex.business.builder.AnnexRelationsResolver;
import com.promaty.rrhh.services.annex.business.builder.CreateAnnexBuilder;
import com.promaty.rrhh.services.annex.business.change.AnnexChangeRuleRegistry;
import com.promaty.rrhh.services.annex.business.validation.AnnexValidation;
import com.promaty.rrhh.services.rexplus.RexPlusClient;
import com.promaty.rrhh.services.rexplus.RexSyncResult;

@Component
public class AnnexRequestHandler implements RequestHandler {

	private static final String CODE_PENDING_VALIDATION = "PENDING_VALIDATION";
	private static final String CODE_APPROVED = "APPROVED";
	private static final String CODE_REJECTED = "REJECTED";
	private static final String CODE_SYNC_ERROR = "SYNC_ERROR";
	private static final String PERMISSION_APPROVE = "annex.approve";
	private static final String PERMISSION_VALIDATE = "annex.validate";
	private static final String PERMISSION_CREATE = "annex.create";
	private static final String PERMISSION_READ = "annex.read";

	private final ObjectMapper objectMapper;
	private final AnnexRepository annexRepository;
	private final ContractRepository contractRepository;
	private final CreateAnnexBuilder createAnnexBuilder;
	private final AnnexRelationsResolver relationsResolver;
	private final AnnexValidation annexValidation;
	private final AnnexChangeRuleRegistry ruleRegistry;
	private final RexPlusClient rexPlusClient;

	public AnnexRequestHandler(
		ObjectMapper objectMapper,
		AnnexRepository annexRepository,
		ContractRepository contractRepository,
		CreateAnnexBuilder createAnnexBuilder,
		AnnexRelationsResolver relationsResolver,
		AnnexValidation annexValidation,
		AnnexChangeRuleRegistry ruleRegistry,
		RexPlusClient rexPlusClient
	) {
		this.objectMapper = objectMapper;
		this.annexRepository = annexRepository;
		this.contractRepository = contractRepository;
		this.createAnnexBuilder = createAnnexBuilder;
		this.relationsResolver = relationsResolver;
		this.annexValidation = annexValidation;
		this.ruleRegistry = ruleRegistry;
		this.rexPlusClient = rexPlusClient;
	}

	@Override
	public RequestEntityType supports() {
		return RequestEntityType.ANNEX;
	}

	@Override
	public String approvePermission() {
		return PERMISSION_APPROVE;
	}

	@Override
	public String validatePermission() {
		return PERMISSION_VALIDATE;
	}

	@Override
	public String createPermission() {
		return PERMISSION_CREATE;
	}

	@Override
	public String readPermission() {
		return PERMISSION_READ;
	}

	@Override
	public Long create(Request request) {
		AnnexPendingDataDto pendingData = deserialize(request.getPendingData());
		return annexRepository.save(createAnnexBuilder.build(pendingData, request.getProject())).getId();
	}

	@Override
	public void applyDecision(Request request, ApprovalLevel level, ApprovalDecision decision) {
		Annex annex = annexRepository.findById(request.getEntityId())
			.orElseThrow(() -> new ResourceNotFoundException("El anexo de la solicitud no existe."));

		if (decision == ApprovalDecision.REJECTED) {
			changeStatus(annex, CODE_REJECTED);
			return;
		}
		if (CODE_APPROVED.equals(annex.getStatus().getCode())) {
			return;
		}
		annexValidation.validateApproval(annex);
		if (level == ApprovalLevel.PROJECT_MANAGER) {
			changeStatus(annex, CODE_PENDING_VALIDATION);
			return;
		}
		applyHrValidation(annex);
	}

	private void applyHrValidation(Annex annex) {
		RexSyncResult sync = rexPlusClient.syncAnnex(annex);
		if (!sync.success()) {
			changeStatus(annex, CODE_SYNC_ERROR);
			return;
		}
		Contract contract = annex.getContract();
		ruleRegistry.get(annex.getTypeCode()).apply(annex, contract);
		contractRepository.save(contract);
		annex.setRexAnnexNumber(sync.externalReferenceNumber());
		changeStatus(annex, CODE_APPROVED);
	}

	private void changeStatus(Annex annex, String code) {
		annex.setStatus(relationsResolver.resolveStatus(code));
		annexRepository.save(annex);
	}

	private AnnexPendingDataDto deserialize(String pendingData) {
		try {
			return objectMapper.readValue(pendingData, AnnexPendingDataDto.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("El pendingData de la solicitud es invalido.", e);
		}
	}
}
