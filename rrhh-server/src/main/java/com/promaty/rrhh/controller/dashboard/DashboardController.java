package com.promaty.rrhh.controller.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.dashboard.BirthdayDto;
import com.promaty.rrhh.dto.dashboard.DashboardSummaryDto;
import com.promaty.rrhh.dto.dashboard.ProjectProgressDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.dashboard.DashboardService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	// Sin permiso propio: cada indicador se entrega segun el permiso de lectura de su dato (docs/rbac.md).
	@GetMapping("/summary")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BaseData<DashboardSummaryDto>> summary(@RequestParam(required = false) Long projectId) {
		return ResponseEntity.ok(BaseData.success(dashboardService.getSummary(projectId)));
	}

	@GetMapping("/birthdays")
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseListData<BirthdayDto>> birthdays(@RequestParam(required = false) Long projectId) {
		return ResponseEntity.ok(BaseListData.of(dashboardService.listBirthdaysOfCurrentMonth(projectId)));
	}

	@GetMapping("/projectProgress")
	@PreAuthorize("hasAuthority('project.read')")
	public ResponseEntity<BaseListData<ProjectProgressDto>> projectProgress(@RequestParam(required = false) Long projectId) {
		return ResponseEntity.ok(BaseListData.of(dashboardService.listProjectProgress(projectId)));
	}
}
