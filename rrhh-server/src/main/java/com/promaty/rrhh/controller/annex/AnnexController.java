package com.promaty.rrhh.controller.annex;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.promaty.rrhh.dto.annex.AnnexDetailDto;
import com.promaty.rrhh.dto.annex.AnnexFilterParams;
import com.promaty.rrhh.dto.annex.AnnexListDto;
import com.promaty.rrhh.dto.annex.UpdateAnnexDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.annex.AnnexService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/annexes")
public class AnnexController {

	private final AnnexService annexService;

	public AnnexController(AnnexService annexService) {
		this.annexService = annexService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority('annex.read')")
	public ResponseEntity<BaseListData<AnnexListDto>> list(
		@ModelAttribute AnnexFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(annexService.listAnnexes(filters, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('annex.read')")
	public ResponseEntity<BaseData<AnnexDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(annexService.getAnnexDetail(id)));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAuthority('annex.update')")
	public ResponseEntity<BaseData<AnnexDetailDto>> update(
		@PathVariable Long id,
		@RequestPart("data") @Valid UpdateAnnexDto dto,
		@RequestPart(value = "files", required = false) List<MultipartFile> files
	) {
		annexService.updateAnnex(id, dto, files);
		return ResponseEntity.ok(BaseData.success(annexService.getAnnexDetail(id)));
	}
}
