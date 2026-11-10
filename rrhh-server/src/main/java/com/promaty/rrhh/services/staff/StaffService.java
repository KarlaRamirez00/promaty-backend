package com.promaty.rrhh.services.staff;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.file.FileAttachmentDto;
import com.promaty.rrhh.dto.staff.CreateStaffDto;
import com.promaty.rrhh.dto.staff.StaffDetailDto;
import com.promaty.rrhh.dto.staff.StaffFilterParams;
import com.promaty.rrhh.dto.staff.StaffListDto;
import com.promaty.rrhh.dto.staff.StaffSelectorOptionDto;
import com.promaty.rrhh.dto.staff.StaffTerminationSelectorOptionDto;
import com.promaty.rrhh.dto.staff.UpdateStaffDto;
import com.promaty.rrhh.dto.staff.UpdateStaffStatusDto;

public interface StaffService {

	Long createStaff(CreateStaffDto dto, MultipartFile file);

	void updateStaff(Long id, UpdateStaffDto dto, MultipartFile file);

	StaffDetailDto updateStaffStatus(Long id, UpdateStaffStatusDto dto);

	Page<StaffListDto> listStaff(StaffFilterParams filters, Pageable pageable);

	StaffDetailDto getStaffDetail(Long id);

	List<StaffSelectorOptionDto> listSelectorOptionsForContract();

	List<StaffTerminationSelectorOptionDto> listSelectorOptionsForTermination();

	FileAttachmentDto uploadAttachment(Long staffId, MultipartFile file);

	void deleteAttachment(Long staffId, Long attachmentId);
}
