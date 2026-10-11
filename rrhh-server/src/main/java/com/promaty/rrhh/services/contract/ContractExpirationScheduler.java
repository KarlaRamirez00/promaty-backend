package com.promaty.rrhh.services.contract;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;

@Component
public class ContractExpirationScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContractExpirationScheduler.class);
	private static final String SUBMODULE_CONTRACT = "contract";
	private static final String CODE_ACTIVE = "ACTIVE";
	private static final String CODE_EXPIRED = "EXPIRED";

	private final ContractRepository contractRepository;
	private final PlatformStatusRepository platformStatusRepository;

	public ContractExpirationScheduler(ContractRepository contractRepository, PlatformStatusRepository platformStatusRepository) {
		this.contractRepository = contractRepository;
		this.platformStatusRepository = platformStatusRepository;
	}

	@Scheduled(cron = "0 5 0 * * *")
	@Transactional
	public void vencerContratosActivos() {
		List<Contract> vencidos = contractRepository.findByStatus_CodeAndEndDateBefore(CODE_ACTIVE, LocalDate.now());
		if (vencidos.isEmpty()) {
			return;
		}
		PlatformStatus estadoVencido = platformStatusRepository.findBySubModuleAndCode(SUBMODULE_CONTRACT, CODE_EXPIRED)
			.orElseThrow(() -> new ResourceNotFoundException("El estado EXPIRED de contrato no está sembrado."));
		vencidos.forEach(contract -> contract.setStatus(estadoVencido));
		contractRepository.saveAll(vencidos);
		LOGGER.info("{} contrato(s) marcados como EXPIRED por vencimiento de endDate.", vencidos.size());
	}
}
