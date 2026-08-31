package com.promaty.rrhh.controller.platformstatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.platformstatus.PlatformStatusService;

@RestController
@RequestMapping("/platformStatuses")
public class PlatformStatusController {

	private final PlatformStatusService platformStatusService;

	public PlatformStatusController(PlatformStatusService platformStatusService) {
		this.platformStatusService = platformStatusService;
	}

	@GetMapping
	public ResponseEntity<BaseListData<PlatformStatusOptionDto>> list(@RequestParam String subModule) {
		return ResponseEntity.ok(BaseListData.of(platformStatusService.listOptionsBySubModule(subModule)));
	}
}
