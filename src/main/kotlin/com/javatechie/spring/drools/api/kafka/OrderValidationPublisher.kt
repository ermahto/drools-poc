package com.javatechie.spring.drools.api.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.javatechie.spring.drools.api.model.OrderMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderValidationPublisher(
	private val kafkaTemplate: KafkaTemplate<String, String>,
	private val objectMapper: ObjectMapper,
	@Value("\${app.kafka.topics.validated}") private val validatedTopic: String,
	@Value("\${app.kafka.topics.rejected}") private val rejectedTopic: String
) {
	private val log = LoggerFactory.getLogger(javaClass)

	fun publish(order: OrderMessage) {
		val topic = if (order.valid) validatedTopic else rejectedTopic
		val payload = objectMapper.writeValueAsString(order)
		kafkaTemplate.send(topic, order.orderId ?: "unknown", payload)
		log.info("Published orderId={} to topic={}", order.orderId, topic)
	}
}
