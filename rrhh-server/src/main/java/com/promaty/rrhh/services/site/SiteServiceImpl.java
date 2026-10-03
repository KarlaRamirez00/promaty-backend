package com.promaty.rrhh.services.site;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.SiteRepository;

@Service
public class SiteServiceImpl implements SiteService {

	private final SiteRepository siteRepository;

	public SiteServiceImpl(SiteRepository siteRepository) {
		this.siteRepository = siteRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return siteRepository.findByActiveTrueOrderByName()
			.stream()
			.map(site -> new CatalogOptionDto(site.getId(), site.getName(), site.getCode()))
			.toList();
	}
}
