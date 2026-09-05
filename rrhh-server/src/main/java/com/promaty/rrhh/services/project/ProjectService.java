package com.promaty.rrhh.services.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.project.CreateProjectDto;
import com.promaty.rrhh.dto.project.ProjectDetailDto;
import com.promaty.rrhh.dto.project.ProjectFilterParams;
import com.promaty.rrhh.dto.project.ProjectListDto;
import com.promaty.rrhh.dto.project.UpdateProjectDto;

public interface ProjectService {

	Long createProject(CreateProjectDto dto);

	void updateProject(Long id, UpdateProjectDto dto);

	Page<ProjectListDto> listProjects(ProjectFilterParams filters, Pageable pageable);

	ProjectDetailDto getProjectDetail(Long id);
}
