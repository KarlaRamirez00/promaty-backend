package com.promaty.rrhh.services.annextype;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.AnnexTypeRepository;
import com.promaty.rrhh.services.annex.business.mapper.AnnexMapper;

@Service
public class AnnexTypeServiceImpl implements AnnexTypeService {

	private final AnnexTypeRepository annexTypeRepository;

	public AnnexTypeServiceImpl(AnnexTypeRepository annexTypeRepository) {
		this.annexTypeRepository = annexTypeRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return annexTypeRepository.findByActiveTrueOrderByName().stream()
			.map(AnnexMapper::toTypeOptionDto)
			.toList();
	}
}
