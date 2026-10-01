package com.promaty.rrhh.services.provincia;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.ProvinciaRepository;

@Service
public class ProvinciaServiceImpl implements ProvinciaService {

	private final ProvinciaRepository provinciaRepository;

	public ProvinciaServiceImpl(ProvinciaRepository provinciaRepository) {
		this.provinciaRepository = provinciaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptionsByRegion(Long regionId) {
		return provinciaRepository.findByRegionIdAndActiveTrueOrderByName(regionId)
			.stream()
			.map(provincia -> new CatalogOptionDto(provincia.getId(), provincia.getName(), provincia.getCode()))
			.toList();
	}
}
