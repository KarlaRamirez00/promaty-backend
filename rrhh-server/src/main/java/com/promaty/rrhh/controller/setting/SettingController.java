package com.promaty.rrhh.controller.setting;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.dto.setting.SettingDto;
import com.promaty.rrhh.dto.setting.UpdateSettingDto;
import com.promaty.rrhh.services.setting.SettingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/settings")
public class SettingController {

	private final SettingService settingService;

	public SettingController(SettingService settingService) {
		this.settingService = settingService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('setting.read')")
	public ResponseEntity<BaseListData<SettingDto>> list() {
		return ResponseEntity.ok(BaseListData.of(settingService.listSettings()));
	}

	@PutMapping("/{key}")
	@PreAuthorize("hasAuthority('setting.update')")
	public ResponseEntity<BaseData<SettingDto>> update(
		@PathVariable String key,
		@Valid @RequestBody UpdateSettingDto dto
	) {
		return ResponseEntity.ok(BaseData.success(settingService.updateSetting(key, dto)));
	}
}
