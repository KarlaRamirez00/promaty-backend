package com.promaty.rrhh.controller.projectspecialty;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.projectspecialty.CreateProjectSpecialtyDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyDetailDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyFilterParams;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyListDto;
import com.promaty.rrhh.dto.projectspecialty.UpdateProjectSpecialtyDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.projectspecialty.ProjectSpecialtyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/projectSpecialties")
public class ProjectSpecialtyController {

	private final ProjectSpecialtyService projectSpecialtyService;

	public ProjectSpecialtyController(ProjectSpecialtyService projectSpecialtyService) {
		this.projectSpecialtyService = projectSpecialtyService;
	}

	@PostMapping
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateProjectSpecialtyDto dto) {
		Long id = projectSpecialtyService.createProjectSpecialty(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	public ResponseEntity<BaseListData<ProjectSpecialtyListDto>> list(
		@ModelAttribute ProjectSpecialtyFilterParams filters,
		Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(projectSpecialtyService.listProjectSpecialties(filters, pageable)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BaseData<ProjectSpecialtyDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(projectSpecialtyService.getProjectSpecialtyDetail(id)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<BaseData<ProjectSpecialtyDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateProjectSpecialtyDto dto
	) {
		projectSpecialtyService.updateProjectSpecialty(id, dto);
		return ResponseEntity.ok(BaseData.success(projectSpecialtyService.getProjectSpecialtyDetail(id)));
	}

	@PatchMapping("/{id}/active")
	public ResponseEntity<BaseData<ProjectSpecialtyDetailDto>> toggleActive(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(projectSpecialtyService.toggleProjectSpecialtyActive(id)));
	}
}
