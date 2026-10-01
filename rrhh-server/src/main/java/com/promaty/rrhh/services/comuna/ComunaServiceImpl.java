package com.promaty.rrhh.services.comuna;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.ComunaRepository;

@Service
public class ComunaServiceImpl implements ComunaService {

	private final ComunaRepository comunaRepository;

	public ComunaServiceImpl(ComunaRepository comunaRepository) {
		this.comunaRepository = comunaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptionsByProvincia(Long provinciaId) {
		return comunaRepository.findByProvinciaIdAndActiveTrueOrderByName(provinciaId)
			.stream()
			.map(comuna -> new CatalogOptionDto(comuna.getId(), comuna.getName(), comuna.getCode()))
			.toList();
	}
}
