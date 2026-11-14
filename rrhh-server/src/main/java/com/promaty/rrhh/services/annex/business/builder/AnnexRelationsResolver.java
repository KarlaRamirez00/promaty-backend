package com.promaty.rrhh.services.annex.business.builder;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.AnnexType;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.JobTitle;
import com.promaty.rrhh.entity.PlatformStatus;
import com.promaty.rrhh.entity.Site;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.AnnexTypeRepository;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.JobTitleRepository;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.repository.SiteRepository;
import com.promaty.rrhh.repository.StaffRepository;

@Component
public class AnnexRelationsResolver {

	private static final String SUBMODULE_ANNEX = "annex";
	private static final String CODE_CONTRACT_ACTIVE = "ACTIVE";

	private final StaffRepository staffRepository;
	private final ContractRepository contractRepository;
	private final AnnexTypeRepository annexTypeRepository;
	private final JobTitleRepository jobTitleRepository;
	private final SiteRepository siteRepository;
	private final PlatformStatusRepository platformStatusRepository;

	public AnnexRelationsResolver(
		StaffRepository staffRepository,
		ContractRepository contractRepository,
		AnnexTypeRepository annexTypeRepository,
		JobTitleRepository jobTitleRepository,
		SiteRepository siteRepository,
		PlatformStatusRepository platformStatusRepository
	) {
		this.staffRepository = staffRepository;
		this.contractRepository = contractRepository;
		this.annexTypeRepository = annexTypeRepository;
		this.jobTitleRepository = jobTitleRepository;
		this.siteRepository = siteRepository;
		this.platformStatusRepository = platformStatusRepository;
	}

	public Staff resolveStaff(Long staffId) {
		return staffRepository.findById(staffId)
			.orElseThrow(() -> new ResourceNotFoundException("El colaborador indicado no existe."));
	}

	public Contract resolveActiveContract(Long staffId) {
		return contractRepository.findByStaffIdAndStatus_Code(staffId, CODE_CONTRACT_ACTIVE)
			.orElseThrow(() -> new ResourceNotFoundException("El colaborador no tiene un contrato activo."));
	}

	public AnnexType resolveAnnexType(Long annexTypeId) {
		return annexTypeRepository.findById(annexTypeId)
			.orElseThrow(() -> new ResourceNotFoundException("El tipo de anexo indicado no existe."));
	}

	public JobTitle resolveJobTitle(Long jobTitleId) {
		if (jobTitleId == null) {
			return null;
		}
		return jobTitleRepository.findById(jobTitleId)
			.orElseThrow(() -> new ResourceNotFoundException("El cargo indicado no existe."));
	}

	public Site resolveSite(Long siteId) {
		if (siteId == null) {
			return null;
		}
		return siteRepository.findById(siteId)
			.orElseThrow(() -> new ResourceNotFoundException("La sucursal indicada no existe."));
	}

	public PlatformStatus resolveStatus(String code) {
		return platformStatusRepository.findBySubModuleAndCode(SUBMODULE_ANNEX, code)
			.orElseThrow(() -> new ResourceNotFoundException("El estado " + code + " de anexo no está sembrado."));
	}
}
