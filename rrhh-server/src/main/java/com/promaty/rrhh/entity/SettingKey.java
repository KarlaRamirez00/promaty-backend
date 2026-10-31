package com.promaty.rrhh.entity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;

@Getter
public enum SettingKey {
	EXPIRING_CONTRACT_DAYS(SettingGroup.CONTRACTS, SettingType.INTEGER, "7", "1", "365",
		"Días para considerar un contrato por vencer");

	private final SettingGroup group;
	private final SettingType type;
	private final String defaultValue;
	private final BigDecimal min;
	private final BigDecimal max;
	private final String label;

	SettingKey(SettingGroup group, SettingType type, String defaultValue, String min, String max, String label) {
		this.group = group;
		this.type = type;
		this.defaultValue = defaultValue;
		this.min = min == null ? null : new BigDecimal(min);
		this.max = max == null ? null : new BigDecimal(max);
		this.label = label;
	}

	public static Optional<SettingKey> fromName(String name) {
		return Arrays.stream(values()).filter(key -> key.name().equals(name)).findFirst();
	}

	public Optional<String> validationError(String rawValue) {
		return switch (type) {
			case BOOLEAN -> booleanError(rawValue);
			case INTEGER, DECIMAL -> numberError(rawValue);
		};
	}

	public String normalize(String rawValue) {
		return switch (type) {
			case BOOLEAN -> rawValue.trim().toLowerCase();
			case INTEGER, DECIMAL -> new BigDecimal(rawValue.trim()).stripTrailingZeros().toPlainString();
		};
	}

	private Optional<String> booleanError(String rawValue) {
		String value = rawValue.trim();
		boolean valid = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
		return valid ? Optional.empty() : Optional.of("Debe ser verdadero o falso.");
	}

	private Optional<String> numberError(String rawValue) {
		BigDecimal number;
		try {
			number = new BigDecimal(rawValue.trim());
		} catch (NumberFormatException e) {
			return Optional.of("Debe ser un número válido.");
		}
		if (type == SettingType.INTEGER && number.stripTrailingZeros().scale() > 0) {
			return Optional.of("Debe ser un número entero.");
		}
		boolean belowMin = min != null && number.compareTo(min) < 0;
		boolean aboveMax = max != null && number.compareTo(max) > 0;
		if (belowMin || aboveMax) {
			return Optional.of("Debe estar entre " + min.toPlainString() + " y " + max.toPlainString() + ".");
		}
		return Optional.empty();
	}
}
