package com.promaty.gateway.config;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayRoutesConfig {

	@Bean
	public RouterFunction<ServerResponse> userServerRoute() {
		return route(path("/users/**").or(path("/roles/**")).or(path("/permissions/**")), http())
			.filter(lb("user-server"));
	}

	@Bean
	public RouterFunction<ServerResponse> authorizerServerRoute() {
		return route(path("/auth/**"), http())
			.filter(lb("authorizer-server"));
	}

	@Bean
	public RouterFunction<ServerResponse> rrhhServerRoute() {
		return route(path("/clients/**")
				.or(path("/projectTypes/**"))
				.or(path("/projectSpecialties/**"))
				.or(path("/projects/**"))
				.or(path("/platformStatuses/**"))
				.or(path("/staff/**"))
				.or(path("/requests/**"))
				.or(path("/registeredSexes/**"))
				.or(path("/maritalStatuses/**"))
				.or(path("/nationalities/**"))
				.or(path("/educationLevels/**"))
				.or(path("/afps/**"))
				.or(path("/healthSystems/**"))
				.or(path("/banks/**"))
				.or(path("/regions/**"))
				.or(path("/provincias/**"))
				.or(path("/comunas/**"))
				.or(path("/companies/**"))
				.or(path("/contractTypes/**"))
				.or(path("/jobTitles/**"))
				.or(path("/sites/**"))
				.or(path("/mealTypes/**"))
				.or(path("/transportTypes/**")), http())
			.filter(lb("rrhh-server"));
	}
}
