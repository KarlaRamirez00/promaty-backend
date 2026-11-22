package com.promaty.user.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.promaty.contracts.auth.UserSessionStatusDto;
import com.promaty.user.dto.response.BaseData;
import com.promaty.user.services.user.UserService;

// Solo para llamadas internas del gateway: InternalApiKeyFilter exige el secreto de servicio.
@RestController
@RequestMapping("/internal/users")
public class UserInternalController {

	private final UserService userService;

	public UserInternalController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/{id}/session")
	public ResponseEntity<BaseData<UserSessionStatusDto>> getSessionStatus(@PathVariable Long id) {
		return ResponseEntity.ok(BaseData.success(userService.getSessionStatus(id)));
	}
}
