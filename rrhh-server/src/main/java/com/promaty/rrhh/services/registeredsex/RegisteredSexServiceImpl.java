package com.promaty.rrhh.services.registeredsex;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.RegisteredSexRepository;

@Service
public class RegisteredSexServiceImpl implements RegisteredSexService {

	private final RegisteredSexRepository registeredSexRepository;

	public RegisteredSexServiceImpl(RegisteredSexRepository registeredSexRepository) {
		this.registeredSexRepository = registeredSexRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return registeredSexRepository.findByActiveTrueOrderByName()
			.stream()
			.map(registeredSex -> new CatalogOptionDto(registeredSex.getId(), registeredSex.getName(), registeredSex.getCode()))
			.toList();
	}
}
