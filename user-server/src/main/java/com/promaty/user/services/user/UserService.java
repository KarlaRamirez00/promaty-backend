package com.promaty.user.services.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.user.dto.user.CreateUserDto;
import com.promaty.user.dto.user.UpdateUserDto;
import com.promaty.user.dto.user.UserDetailDto;
import com.promaty.user.dto.user.UserFilterParams;
import com.promaty.user.dto.user.UserListDto;

public interface UserService {

	Long createUser(CreateUserDto dto);

	void updateUser(Long id, UpdateUserDto dto);

	Page<UserListDto> listUsers(UserFilterParams filters, Pageable pageable);

	UserDetailDto getUserDetail(Long id);

	UserDetailDto toggleUserActive(Long id);
}
