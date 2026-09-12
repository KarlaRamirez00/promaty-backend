package com.promaty.user.services.shared;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.promaty.user.dto.shared.Action;

@Component
public class ActionsResolver {

	public List<Action> resolve(Collection<String> authorities, Map<String, Action> reglas) {
		return reglas.entrySet().stream()
			.filter(regla -> authorities.contains(regla.getKey()))
			.map(Map.Entry::getValue)
			.toList();
	}
}
