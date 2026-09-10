package com.promaty.rrhh.controller.project;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.project.CreateProjectDto;
import com.promaty.rrhh.dto.project.ProjectDetailDto;
import com.promaty.rrhh.dto.project.ProjectFilterParams;
import com.promaty.rrhh.dto.project.ProjectListDto;
import com.promaty.rrhh.dto.project.UpdateProjectDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.project.ProjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects")
public class ProjectController {

	private final ProjectService projectService;

	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('project.create')")
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateProjectDto dto) {
		Long id = projectService.createProject(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('project.read')")
	public ResponseEntity<BaseListData<ProjectListDto>> list(
		@ModelAttribute ProjectFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(projectService.listProjects(filters, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('project.read')")
	public ResponseEntity<BaseData<ProjectDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(projectService.getProjectDetail(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('project.update')")
	public ResponseEntity<BaseData<ProjectDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateProjectDto dto
	) {
		projectService.updateProject(id, dto);
		return ResponseEntity.ok(BaseData.success(projectService.getProjectDetail(id)));
	}
}
