package com.promaty.rrhh.services.maritalstatus;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.MaritalStatusRepository;

@Service
public class MaritalStatusServiceImpl implements MaritalStatusService {

	private final MaritalStatusRepository maritalStatusRepository;

	public MaritalStatusServiceImpl(MaritalStatusRepository maritalStatusRepository) {
		this.maritalStatusRepository = maritalStatusRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return maritalStatusRepository.findByActiveTrueOrderByName()
			.stream()
			.map(maritalStatus -> new CatalogOptionDto(maritalStatus.getId(), maritalStatus.getName(), maritalStatus.getCode()))
			.toList();
	}
}
