package com.promaty.authorizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AuthorizerServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthorizerServerApplication.class, args);
	}

}
