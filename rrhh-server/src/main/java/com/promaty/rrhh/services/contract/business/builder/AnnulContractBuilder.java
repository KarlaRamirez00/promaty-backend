package com.promaty.rrhh.services.contract.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.StaffRepository;

@Component
public class AnnulContractBuilder {

	private static final String SUBMODULE_CONTRACT = "contract";
	private static final String SUBMODULE_REQUEST = "request";
	private static final String SUBMODULE_STAFF = "staff";
	private static final String CODE_ANNULLED = "ANNULLED";
	private static final String CODE_STAFF_ACTIVE = "ACTIVE";
	private static final String CODE_STAFF_PENDING_CONTRACT = "PENDING_CONTRACT";

	private final ContractRepository contractRepository;
	private final StaffRepository staffRepository;
	private final RequestRepository requestRepository;
	private final PlatformStatusRepository platformStatusRepository;

	public AnnulContractBuilder(
		ContractRepository contractRepository,
		StaffRepository staffRepository,
		RequestRepository requestRepository,
		PlatformStatusRepository platformStatusRepository
	) {
		this.contractRepository = contractRepository;
		this.staffRepository = staffRepository;
		this.requestRepository = requestRepository;
		this.platformStatusRepository = platformStatusRepository;
	}

	public void apply(Contract contract, String reason) {
		contract.setAnnulmentReason(reason);
		contract.setStatus(resolveStatus(SUBMODULE_CONTRACT, CODE_ANNULLED));
		contractRepository.save(contract);
		restoreStaff(contract);
		annulRequest(contract.getId());
	}

	private void restoreStaff(Contract contract) {
		Staff staff = contract.getStaff();
		// Un bloqueo aplicado después de aprobar el contrato no se debe pisar al anularlo.
		if (!CODE_STAFF_ACTIVE.equals(staff.getStatus().getCode())) {
			return;
		}
		PlatformStatus previous = contract.getStaffStatusBeforeActivation();
		staff.setStatus(previous != null ? previous : resolveStatus(SUBMODULE_STAFF, CODE_STAFF_PENDING_CONTRACT));
		staffRepository.save(staff);
	}

	private void annulRequest(Long contractId) {
		requestRepository.findByEntityTypeAndEntityId(RequestEntityType.CONTRACT, contractId)
			.ifPresent(request -> {
				request.setStatus(resolveStatus(SUBMODULE_REQUEST, CODE_ANNULLED));
				requestRepository.save(request);
			});
	}

	private PlatformStatus resolveStatus(String subModule, String code) {
		return platformStatusRepository.findBySubModuleAndCode(subModule, code)
			.orElseThrow(() -> new ResourceNotFoundException("El estado " + code + " de " + subModule + " no está sembrado."));
	}
}
