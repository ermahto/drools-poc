package com.drools.cli

import org.drools.model.codegen.ExecutableModelProject
import org.kie.api.KieServices
import org.kie.api.builder.KieModule
import org.kie.api.builder.Message
import org.kie.api.builder.ReleaseId
import org.kie.api.runtime.KieContainer
import org.kie.internal.io.ResourceFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DroolsConfig {

	@Bean
	fun kieContainer(): KieContainer {
		val kieServices = KieServices.Factory.get()
		val kieRepository = kieServices.repository
		kieRepository.addKieModule(object : KieModule {
			override fun getReleaseId(): ReleaseId = kieRepository.defaultReleaseId
		})

		val kieFileSystem = kieServices.newKieFileSystem()
		kieFileSystem.write(ResourceFactory.newClassPathResource("rules/order-validation.drl"))

		val kieBuilder = kieServices.newKieBuilder(kieFileSystem)
		kieBuilder.buildAll(ExecutableModelProject::class.java)

		val errors = kieBuilder.results.getMessages(Message.Level.ERROR)
		if (errors.isNotEmpty()) {
			throw IllegalStateException("Drools rule compilation failed: $errors")
		}

		return kieServices.newKieContainer(kieBuilder.kieModule.releaseId)
	}
}
