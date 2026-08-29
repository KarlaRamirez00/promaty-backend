package com.promaty.rrhh.services.projectspecialty;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.projectspecialty.CreateProjectSpecialtyDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyDetailDto;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyFilterParams;
import com.promaty.rrhh.dto.projectspecialty.ProjectSpecialtyListDto;
import com.promaty.rrhh.dto.projectspecialty.UpdateProjectSpecialtyDto;

public interface ProjectSpecialtyService {

	Long createProjectSpecialty(CreateProjectSpecialtyDto dto);

	void updateProjectSpecialty(Long id, UpdateProjectSpecialtyDto dto);

	Page<ProjectSpecialtyListDto> listProjectSpecialties(ProjectSpecialtyFilterParams filters, Pageable pageable);

	ProjectSpecialtyDetailDto getProjectSpecialtyDetail(Long id);

	ProjectSpecialtyDetailDto toggleProjectSpecialtyActive(Long id);
}
