package com.promaty.authorizer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "internal-api.secret=test-internal-secret-0123456789-abcdef")
class AuthorizerServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
