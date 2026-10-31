package com.promaty.rrhh.dto.setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettingDto {

	private String key;
	private String group;
	private String groupLabel;
	private String label;
	private String type;
	private String value;
	private String defaultValue;
	private BigDecimal min;
	private BigDecimal max;
	private LocalDateTime updatedAt;
	private String updatedBy;
}
