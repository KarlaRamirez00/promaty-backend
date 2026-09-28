package com.promaty.rrhh.services.afp;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.AfpRepository;

@Service
public class AfpServiceImpl implements AfpService {

	private final AfpRepository afpRepository;

	public AfpServiceImpl(AfpRepository afpRepository) {
		this.afpRepository = afpRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return afpRepository.findByActiveTrueOrderByName()
			.stream()
			.map(afp -> new CatalogOptionDto(afp.getId(), afp.getName(), afp.getCode()))
			.toList();
	}
}
