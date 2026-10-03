package com.promaty.rrhh.controller.jobtitle;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.shared.CatalogOptionDto;
import com.promaty.rrhh.services.jobtitle.JobTitleService;

@RestController
@RequestMapping("/jobTitles")
public class JobTitleController {

	private final JobTitleService jobTitleService;

	public JobTitleController(JobTitleService jobTitleService) {
		this.jobTitleService = jobTitleService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('contract.read')")
	public ResponseEntity<BaseListData<CatalogOptionDto>> list() {
		return ResponseEntity.ok(BaseListData.of(jobTitleService.listOptions()));
	}
}
