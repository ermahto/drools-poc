package com.javatechie.spring.drools.api.config

import org.kie.api.KieServices
import org.kie.api.builder.KieModule
import org.kie.api.builder.Message
import org.kie.api.builder.ReleaseId
import org.kie.api.runtime.KieContainer
import org.kie.internal.io.ResourceFactory

object DroolsContainerFactory {

	fun create(ruleFile: String = "rules/order-validation.drl"): KieContainer {
		val kieServices = KieServices.Factory.get()
		val kieRepository = kieServices.repository
		kieRepository.addKieModule(object : KieModule {
			override fun getReleaseId(): ReleaseId = kieRepository.defaultReleaseId
		})

		val kieFileSystem = kieServices.newKieFileSystem()
		kieFileSystem.write(ResourceFactory.newClassPathResource(ruleFile))

		val kieBuilder = kieServices.newKieBuilder(kieFileSystem)
		kieBuilder.buildAll()

		val errors = kieBuilder.results.getMessages(Message.Level.ERROR)
		if (errors.isNotEmpty()) {
			throw IllegalStateException("Drools rule compilation failed: $errors")
		}

		return kieServices.newKieContainer(kieBuilder.kieModule.releaseId)
	}
}
