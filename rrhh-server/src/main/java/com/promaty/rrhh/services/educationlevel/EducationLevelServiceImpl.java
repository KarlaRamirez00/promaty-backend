package com.promaty.rrhh.services.educationlevel;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.EducationLevelRepository;

@Service
public class EducationLevelServiceImpl implements EducationLevelService {

	private final EducationLevelRepository educationLevelRepository;

	public EducationLevelServiceImpl(EducationLevelRepository educationLevelRepository) {
		this.educationLevelRepository = educationLevelRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return educationLevelRepository.findByActiveTrueOrderByName()
			.stream()
			.map(educationLevel -> new CatalogOptionDto(educationLevel.getId(), educationLevel.getName(), educationLevel.getCode()))
			.toList();
	}
}
