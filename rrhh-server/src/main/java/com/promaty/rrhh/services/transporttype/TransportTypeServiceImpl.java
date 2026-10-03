package com.promaty.rrhh.services.transporttype;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.TransportTypeRepository;

@Service
public class TransportTypeServiceImpl implements TransportTypeService {

	private final TransportTypeRepository transportTypeRepository;

	public TransportTypeServiceImpl(TransportTypeRepository transportTypeRepository) {
		this.transportTypeRepository = transportTypeRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return transportTypeRepository.findByActiveTrueOrderByName()
			.stream()
			.map(transportType -> new CatalogOptionDto(transportType.getId(), transportType.getName(), transportType.getCode()))
			.toList();
	}
}
