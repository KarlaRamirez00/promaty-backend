package com.promaty.rrhh.services.nationality;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.NationalityRepository;

@Service
public class NationalityServiceImpl implements NationalityService {

	private final NationalityRepository nationalityRepository;

	public NationalityServiceImpl(NationalityRepository nationalityRepository) {
		this.nationalityRepository = nationalityRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return nationalityRepository.findByActiveTrueOrderByName()
			.stream()
			.map(nationality -> new CatalogOptionDto(nationality.getId(), nationality.getName(), nationality.getCode()))
			.toList();
	}
}
