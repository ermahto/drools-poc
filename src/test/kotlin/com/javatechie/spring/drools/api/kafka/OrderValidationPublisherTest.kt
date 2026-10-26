package com.javatechie.spring.drools.api.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.javatechie.spring.drools.api.model.OrderMessage
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.kafka.core.KafkaTemplate

class OrderValidationPublisherTest {

	private val objectMapper = jacksonObjectMapper()

	@Suppress("UNCHECKED_CAST")
	private val kafkaTemplate = mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>

	private val publisher = OrderValidationPublisher(
		kafkaTemplate,
		objectMapper,
		"order-validated",
		"order-rejected"
	)

	@Test
	fun validOrderIsPublishedToValidatedTopic() {
		val order = OrderMessage(orderId = "ORD-1001", valid = true, validationStatus = "VALIDATED")

		publisher.publish(order)

		verify(kafkaTemplate).send(
			eq("order-validated"),
			eq("ORD-1001"),
			eq(objectMapper.writeValueAsString(order))
		)
	}

	@Test
	fun rejectedOrderIsPublishedToRejectedTopic() {
		val order = OrderMessage(orderId = "ORD-BAD", valid = false, validationStatus = "REJECTED")
		order.rejectionReasons.add("orderId is mandatory")

		publisher.publish(order)

		verify(kafkaTemplate).send(
			eq("order-rejected"),
			eq("ORD-BAD"),
			eq(objectMapper.writeValueAsString(order))
		)
	}
}
