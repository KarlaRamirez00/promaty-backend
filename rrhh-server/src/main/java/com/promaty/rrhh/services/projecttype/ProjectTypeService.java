package com.promaty.rrhh.services.projecttype;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.projecttype.CreateProjectTypeDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeDetailDto;
import com.promaty.rrhh.dto.projecttype.ProjectTypeFilterParams;
import com.promaty.rrhh.dto.projecttype.ProjectTypeListDto;
import com.promaty.rrhh.dto.projecttype.UpdateProjectTypeDto;

public interface ProjectTypeService {

	Long createProjectType(CreateProjectTypeDto dto);

	void updateProjectType(Long id, UpdateProjectTypeDto dto);

	Page<ProjectTypeListDto> listProjectTypes(ProjectTypeFilterParams filters, Pageable pageable);

	ProjectTypeDetailDto getProjectTypeDetail(Long id);

	ProjectTypeDetailDto toggleProjectTypeActive(Long id);
}
