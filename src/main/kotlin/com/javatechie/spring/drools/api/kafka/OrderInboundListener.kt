package com.javatechie.spring.drools.api.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.javatechie.spring.drools.api.model.OrderMessage
import com.javatechie.spring.drools.api.service.OrderValidationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderInboundListener(
	private val objectMapper: ObjectMapper,
	private val validationService: OrderValidationService,
	private val publisher: OrderValidationPublisher
) {
	private val log = LoggerFactory.getLogger(javaClass)

	@KafkaListener(
		topics = ["\${app.kafka.topics.inbound}"],
		autoStartup = "\${app.kafka.listener.auto-startup:true}"
	)
	fun onMessage(payload: String) {
		log.info("Received inbound order payload={}", payload)
		try {
			val order = objectMapper.readValue(payload, OrderMessage::class.java)
			validationService.validate(order)
			publisher.publish(order)
		} catch (ex: Exception) {
			log.error("Failed to process inbound payload", ex)
			val rejected = OrderMessage()
			rejected.reject("Unable to process payload: ${ex.message}")
			rejected.applyValidationStatus()
			publisher.publish(rejected)
		}
	}
}
