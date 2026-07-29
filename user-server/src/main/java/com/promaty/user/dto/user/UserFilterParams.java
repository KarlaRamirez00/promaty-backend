package com.promaty.user.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFilterParams {

	private Long roleId;
	private Boolean status;
	private String search;
}
