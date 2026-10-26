package com.javatechie.spring.drools.api.config

import org.kie.api.runtime.KieContainer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DroolsConfig {

	@Bean
	fun kieContainer(
		@Value("\${drools.rule-file:rules/order-validation.drl}") ruleFile: String
	): KieContainer = DroolsContainerFactory.create(ruleFile)
}
