package com.promaty.rrhh.controller.mealtype;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.mealtype.MealTypeService;

@RestController
@RequestMapping("/mealTypes")
public class MealTypeController {

	private final MealTypeService mealTypeService;

	public MealTypeController(MealTypeService mealTypeService) {
		this.mealTypeService = mealTypeService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('contract.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(mealTypeService.listOptions()));
	}
}
