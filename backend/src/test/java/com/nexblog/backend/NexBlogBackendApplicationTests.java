package com.nexblog.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:NexBlogTest;DB_CLOSE_DELAY=-1")
class NexBlogBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
