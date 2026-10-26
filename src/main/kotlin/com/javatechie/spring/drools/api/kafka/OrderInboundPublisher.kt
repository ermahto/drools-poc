package com.javatechie.spring.drools.api.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.javatechie.spring.drools.api.model.OrderMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderInboundPublisher(
	private val kafkaTemplate: KafkaTemplate<String, String>,
	private val objectMapper: ObjectMapper,
	@Value("\${app.kafka.topics.inbound}") private val inboundTopic: String
) {
	private val log = LoggerFactory.getLogger(javaClass)

	fun publish(order: OrderMessage): String {
		val payload = objectMapper.writeValueAsString(order)
		kafkaTemplate.send(inboundTopic, order.orderId ?: "unknown", payload).get()
		log.info("Published orderId={} to topic={}", order.orderId, inboundTopic)
		return payload
	}
}
