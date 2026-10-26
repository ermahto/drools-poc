package com.javatechie.spring.drools.api.web

import com.javatechie.spring.drools.api.kafka.OrderInboundPublisher
import com.javatechie.spring.drools.api.model.OrderMessage
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(
	private val inboundPublisher: OrderInboundPublisher
) {

	@PostMapping("/orders")
	@ResponseStatus(HttpStatus.ACCEPTED)
	fun submit(@RequestBody order: OrderMessage): Map<String, Any?> {
		inboundPublisher.publish(order)
		return mapOf(
			"message" to "Published to order-inbound. Drools will send the result to order-validated or order-rejected.",
			"orderId" to order.orderId,
			"payload" to order
		)
	}
}
