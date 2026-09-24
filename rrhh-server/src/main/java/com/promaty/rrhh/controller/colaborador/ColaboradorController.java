package com.promaty.rrhh.controller.colaborador;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.colaborador.ColaboradorDetailDto;
import com.promaty.rrhh.dto.colaborador.ColaboradorFilterParams;
import com.promaty.rrhh.dto.colaborador.ColaboradorListDto;
import com.promaty.rrhh.dto.colaborador.CreateColaboradorDto;
import com.promaty.rrhh.dto.colaborador.UpdateColaboradorDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.colaborador.ColaboradorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/colaboradores")
public class ColaboradorController {

	private final ColaboradorService colaboradorService;

	public ColaboradorController(ColaboradorService colaboradorService) {
		this.colaboradorService = colaboradorService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority('colaborador.create')")
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateColaboradorDto dto) {
		Long id = colaboradorService.createColaborador(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	@PreAuthorize("hasAuthority('colaborador.read')")
	public ResponseEntity<BaseListData<ColaboradorListDto>> list(
		@ModelAttribute ColaboradorFilterParams filters,
		@PageableDefault(sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(colaboradorService.listColaboradores(filters, pageable)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAuthority('colaborador.read')")
	public ResponseEntity<BaseData<ColaboradorDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(colaboradorService.getColaboradorDetail(id)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('colaborador.update')")
	public ResponseEntity<BaseData<ColaboradorDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateColaboradorDto dto
	) {
		colaboradorService.updateColaborador(id, dto);
		return ResponseEntity.ok(BaseData.success(colaboradorService.getColaboradorDetail(id)));
	}

	@PatchMapping("/{id}/active")
	@PreAuthorize("hasAuthority('colaborador.active')")
	public ResponseEntity<BaseData<ColaboradorDetailDto>> toggleActive(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(colaboradorService.toggleColaboradorActive(id)));
	}
}
