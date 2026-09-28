package com.promaty.rrhh.services.healthsystem;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.HealthSystemRepository;

@Service
public class HealthSystemServiceImpl implements HealthSystemService {

	private final HealthSystemRepository healthSystemRepository;

	public HealthSystemServiceImpl(HealthSystemRepository healthSystemRepository) {
		this.healthSystemRepository = healthSystemRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return healthSystemRepository.findByActiveTrueOrderByName()
			.stream()
			.map(healthSystem -> new CatalogOptionDto(healthSystem.getId(), healthSystem.getName(), healthSystem.getCode()))
			.toList();
	}
}
