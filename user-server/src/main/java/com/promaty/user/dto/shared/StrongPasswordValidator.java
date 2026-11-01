package com.promaty.user.dto.shared;

import java.util.List;
import java.util.function.Predicate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

	static final int MIN_LENGTH = 8;
	static final int MAX_LENGTH = 12;

	private record Rule(Predicate<String> isSatisfiedBy, String requirement) {
	}

	private static final List<Rule> RULES = List.of(
		new Rule(
			password -> password.length() >= MIN_LENGTH && password.length() <= MAX_LENGTH,
			"entre " + MIN_LENGTH + " y " + MAX_LENGTH + " caracteres"
		),
		new Rule(password -> password.chars().anyMatch(Character::isUpperCase), "una mayuscula"),
		new Rule(password -> password.chars().anyMatch(Character::isLowerCase), "una minuscula"),
		new Rule(password -> password.chars().anyMatch(Character::isDigit), "un numero"),
		new Rule(
			password -> password.chars().anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c)),
			"un caracter especial"
		)
	);

	@Override
	public boolean isValid(String password, ConstraintValidatorContext context) {
		if (password == null) {
			return true;
		}
		List<String> unmet = RULES.stream()
			.filter(rule -> !rule.isSatisfiedBy().test(password))
			.map(Rule::requirement)
			.toList();
		if (unmet.isEmpty()) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate("La contrasena debe tener " + joinWithAnd(unmet) + ".")
			.addConstraintViolation();
		return false;
	}

	private static String joinWithAnd(List<String> items) {
		if (items.size() == 1) {
			return items.get(0);
		}
		String allButLast = String.join(", ", items.subList(0, items.size() - 1));
		return allButLast + " y " + items.get(items.size() - 1);
	}
}
