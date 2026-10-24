package com.promaty.rrhh.services.staff;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.dto.staff.StaffListDto;
import com.promaty.rrhh.dto.staff.StaffSelectorOptionDto;
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.dto.staff.UpdateStaffStatusDto;

public interface StaffService {

	Long createStaff(CreateStaffDto dto);

	void updateStaff(Long id, UpdateStaffDto dto);

	StaffDetailDto updateStaffStatus(Long id, UpdateStaffStatusDto dto);

	Page<StaffListDto> listStaff(StaffFilterParams filters, Pageable pageable);

	StaffDetailDto getStaffDetail(Long id);

	List<StaffSelectorOptionDto> listSelectorOptionsForContract();

	List<StaffSelectorOptionDto> listSelectorOptionsForTermination();
}
