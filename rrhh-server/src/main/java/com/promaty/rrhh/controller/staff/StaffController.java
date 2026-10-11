package com.promaty.rrhh.controller.staff;

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

import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.dto.staff.StaffListDto;
import com.promaty.rrhh.dto.staff.StaffSelectorOptionDto;
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.services.staff.StaffService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/staff")
public class StaffController {

	private final StaffService staffService;

	public StaffController(StaffService staffService) {
		this.staffService = staffService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('staff.create')")
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateStaffDto dto) {
		Long id = staffService.createStaff(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseListData<StaffListDto>> list(
		@ModelAttribute StaffFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(staffService.listStaff(filters, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('staff.read')")
	public ResponseEntity<BaseData<StaffDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(staffService.getStaffDetail(id)));
	}

	@GetMapping("/selector")
	@PreAuthorize("hasAuthority('contract.create')")
	public ResponseEntity<BaseListData<StaffSelectorOptionDto>> selectorForContract() {
		return ResponseEntity.ok(BaseListData.of(staffService.listSelectorOptionsForContract()));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('staff.update')")
	public ResponseEntity<BaseData<StaffDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateStaffDto dto
	) {
		staffService.updateStaff(id, dto);
		return ResponseEntity.ok(BaseData.success(staffService.getStaffDetail(id)));
	}
}
