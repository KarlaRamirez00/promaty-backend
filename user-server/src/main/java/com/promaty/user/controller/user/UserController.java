package com.promaty.user.controller.user;

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

import com.promaty.user.dto.response.BaseData;
import com.promaty.user.dto.response.BaseListData;
import com.promaty.user.dto.user.CreateUserDto;
import com.promaty.user.dto.user.UpdateUserDto;
import com.promaty.user.dto.user.UserDetailDto;
import com.promaty.user.dto.user.UserFilterParams;
import com.promaty.user.dto.user.UserListDto;
import com.promaty.user.services.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public ResponseEntity<BaseData<Long>> create(@Valid @RequestBody CreateUserDto dto) {
		Long id = userService.createUser(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseData.success(id));
	}

	@GetMapping
	public ResponseEntity<BaseListData<UserListDto>> list(
		@ModelAttribute UserFilterParams filters,
		Pageable pageable
	) {
		return ResponseEntity.ok(BaseListData.of(userService.listUsers(filters, pageable)));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BaseData<UserDetailDto>> detail(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(userService.getUserDetail(id)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<BaseData<UserDetailDto>> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateUserDto dto
	) {
		userService.updateUser(id, dto);
		return ResponseEntity.ok(BaseData.success(userService.getUserDetail(id)));
	}

	@PatchMapping("/{id}/active")
	public ResponseEntity<BaseData<UserDetailDto>> toggleActive(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(userService.toggleUserActive(id)));
	}
}
