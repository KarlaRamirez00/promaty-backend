package com.promaty.rrhh.services.platformstatus;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.repository.PlatformStatusRepository;
import com.promaty.rrhh.services.platformstatus.business.mapper.PlatformStatusMapper;

@Service
public class PlatformStatusServiceImpl implements PlatformStatusService {

	private final PlatformStatusRepository platformStatusRepository;

	public PlatformStatusServiceImpl(PlatformStatusRepository platformStatusRepository) {
		this.platformStatusRepository = platformStatusRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<PlatformStatusOptionDto> listOptionsBySubModule(String subModule) {
		return platformStatusRepository.findBySubModuleAndActiveTrueOrderBySortOrder(subModule)
			.stream()
			.map(PlatformStatusMapper::toOptionDto)
			.toList();
	}
}
