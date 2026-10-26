package com.javatechie.spring.drools.api

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = ["app.kafka.listener.auto-startup=false"])
class SpringDroolsApplicationTests {

	@Test
	fun contextLoads() {
	}
}
