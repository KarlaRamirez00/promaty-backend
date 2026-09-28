package com.promaty.rrhh.controller.educationlevel;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.educationlevel.EducationLevelService;

@RestController
@RequestMapping("/educationLevels")
public class EducationLevelController {

	private final EducationLevelService educationLevelService;

	public EducationLevelController(EducationLevelService educationLevelService) {
		this.educationLevelService = educationLevelService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(educationLevelService.listOptions()));
	}
}
