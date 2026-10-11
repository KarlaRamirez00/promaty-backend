package com.promaty.rrhh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RrhhServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(RrhhServerApplication.class, args);
	}

}
