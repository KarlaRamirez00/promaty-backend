package com.promaty.rrhh.services.mealtype;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.repository.MealTypeRepository;

@Service
public class MealTypeServiceImpl implements MealTypeService {

	private final MealTypeRepository mealTypeRepository;

	public MealTypeServiceImpl(MealTypeRepository mealTypeRepository) {
		this.mealTypeRepository = mealTypeRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CatalogOptionDto> listOptions() {
		return mealTypeRepository.findByActiveTrueOrderByName()
			.stream()
			.map(mealType -> new CatalogOptionDto(mealType.getId(), mealType.getName(), mealType.getCode()))
			.toList();
	}
}
