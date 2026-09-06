package com.promaty.rrhh.controller.projecttype;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import com.promaty.rrhh.dto.projecttype.CreateProjectTypeDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeDetailDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeFilterParams;
import com.promaty.rrhh.dto.projecttype.ProjectTypeListDto;
import com.promaty.rrhh.dto.projecttype.UpdateProjectTypeDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.projecttype.ProjectTypeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/projectTypes")
public class ProjectTypeController {

	private final ProjectTypeService projectTypeService;

	public ProjectTypeController(ProjectTypeService projectTypeService) {
		this.projectTypeService = projectTypeService;
	}

	@PostMapping
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateProjectTypeDto dto) {
		Long id = projectTypeService.createProjectType(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	public ResponseEntity<BaseListData<ProjectTypeListDto>> list(
		@ModelAttribute ProjectTypeFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(projectTypeService.listProjectTypes(filters, pageable)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BaseData<ProjectTypeDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(projectTypeService.getProjectTypeDetail(id)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<BaseData<ProjectTypeDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateProjectTypeDto dto
	) {
		projectTypeService.updateProjectType(id, dto);
		return ResponseEntity.ok(BaseData.success(projectTypeService.getProjectTypeDetail(id)));
	}

	@PatchMapping("/{id}/active")
	public ResponseEntity<BaseData<ProjectTypeDetailDto>> toggleActive(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(projectTypeService.toggleProjectTypeActive(id)));
	}
}
