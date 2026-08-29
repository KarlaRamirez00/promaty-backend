package com.promaty.rrhh.controller.client;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.rrhh.dto.client.ClientDetailDto;
import com.promaty.rrhh.dto.client.ClientFilterParams;
import com.promaty.rrhh.dto.client.ClientListDto;
import com.promaty.rrhh.dto.client.CreateClientDto;
import com.promaty.rrhh.dto.client.UpdateClientDto;
import com.promaty.rrhh.dto.response.BaseData;
import com.promaty.rrhh.dto.response.BaseListData;
import com.promaty.rrhh.services.client.ClientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/clients")
public class ClientController {

	private final ClientService clientService;

	public ClientController(ClientService clientService) {
		this.clientService = clientService;
	}

	@PostMapping
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateClientDto dto) {
		Long id = clientService.createClient(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	public ResponseEntity<BaseListData<ClientListDto>> list(
		@ModelAttribute ClientFilterParams filters,
		Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(clientService.listClients(filters, pageable)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BaseData<ClientDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(clientService.getClientDetail(id)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<BaseData<ClientDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateClientDto dto
	) {
		clientService.updateClient(id, dto);
		return ResponseEntity.ok(BaseData.success(clientService.getClientDetail(id)));
	}

	@PatchMapping("/{id}/active")
	public ResponseEntity<BaseData<ClientDetailDto>> toggleActive(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(clientService.toggleClientActive(id)));
	}
}
