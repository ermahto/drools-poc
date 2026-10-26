package com.javatechie.spring.drools.api.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.javatechie.spring.drools.api.config.DroolsContainerFactory
import com.javatechie.spring.drools.api.model.OrderMessage
import com.javatechie.spring.drools.api.service.OrderValidationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import org.springframework.kafka.core.KafkaTemplate

class OrderInboundListenerTest {

	private val objectMapper = jacksonObjectMapper()
	private val validationService = OrderValidationService(DroolsContainerFactory.create())
	private val publisher = RecordingPublisher()
	private val listener = OrderInboundListener(objectMapper, validationService, publisher)

	@Test
	fun validPayloadIsValidatedAndPublished() {
		val payload = """
			{
			  "orderId": "ORD-1001",
			  "customerName": "Alex Kumar",
			  "country": "US",
			  "orderType": "STANDARD",
			  "amount": 5000
			}
		""".trimIndent()

		listener.onMessage(payload)

		assertThat(publisher.published).hasSize(1)
		assertThat(publisher.published[0].valid).isTrue()
		assertThat(publisher.published[0].validationStatus).isEqualTo("VALIDATED")
	}

	@Test
	fun invalidPayloadIsRejectedAndPublished() {
		val payload = """
			{
			  "orderId": "ORD-BAD",
			  "customerName": "Alex Kumar",
			  "country": "FR",
			  "orderType": "STANDARD",
			  "amount": 15000
			}
		""".trimIndent()

		listener.onMessage(payload)

		assertThat(publisher.published).hasSize(1)
		assertThat(publisher.published[0].valid).isFalse()
		assertThat(publisher.published[0].validationStatus).isEqualTo("REJECTED")
		assertThat(publisher.published[0].rejectionReasons).isNotEmpty()
	}

	@Test
	fun malformedJsonIsRejected() {
		listener.onMessage("{not-json")

		assertThat(publisher.published).hasSize(1)
		assertThat(publisher.published[0].valid).isFalse()
		assertThat(publisher.published[0].rejectionReasons).anyMatch { it.startsWith("Unable to process payload:") }
	}

	@Suppress("UNCHECKED_CAST")
	private class RecordingPublisher : OrderValidationPublisher(
		mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>,
		jacksonObjectMapper(),
		"order-validated",
		"order-rejected"
	) {
		val published = mutableListOf<OrderMessage>()

		override fun publish(order: OrderMessage) {
			published.add(order)
		}
	}
}
