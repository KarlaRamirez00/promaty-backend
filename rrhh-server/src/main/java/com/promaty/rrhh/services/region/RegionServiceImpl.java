package com.promaty.rrhh.services.region;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.RegionRepository;

@Service
public class RegionServiceImpl implements RegionService {

	private final RegionRepository regionRepository;

	public RegionServiceImpl(RegionRepository regionRepository) {
		this.regionRepository = regionRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return regionRepository.findByActiveTrueOrderByName()
			.stream()
			.map(region -> new CatalogOptionDto(region.getId(), region.getName(), region.getCode()))
			.toList();
	}
}
