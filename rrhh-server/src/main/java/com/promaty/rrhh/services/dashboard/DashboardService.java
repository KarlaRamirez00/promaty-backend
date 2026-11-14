package com.promaty.rrhh.services.dashboard;

import java.util.List;

import com.promaty.rrhh.dto.dashboard.BirthdayDto;
import com.promaty.rrhh.dto.dashboard.DashboardSummaryDto;
import com.promaty.rrhh.dto.dashboard.ProjectProgressDto;

public interface DashboardService {

	DashboardSummaryDto getSummary(Long projectId);

	List<BirthdayDto> listBirthdaysOfCurrentMonth(Long projectId);

	List<ProjectProgressDto> listProjectProgress(Long projectId);
}
