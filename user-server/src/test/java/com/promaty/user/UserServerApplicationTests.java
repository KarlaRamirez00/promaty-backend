package com.promaty.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.promaty.user.support.TestInternalApi;

@SpringBootTest(properties = "internal-api.secret=" + TestInternalApi.SECRET)
class UserServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
