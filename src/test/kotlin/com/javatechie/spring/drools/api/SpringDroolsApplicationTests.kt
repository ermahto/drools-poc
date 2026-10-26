package com.javatechie.spring.drools.api

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = ["app.kafka.listener.auto-startup=false"])
class SpringDroolsApplicationTests {

	@Test
	fun contextLoads() {
	}
}
