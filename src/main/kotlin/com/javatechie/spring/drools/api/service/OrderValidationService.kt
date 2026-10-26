package com.javatechie.spring.drools.api.service

import com.javatechie.spring.drools.api.model.OrderMessage
import org.kie.api.runtime.KieContainer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class OrderValidationService(private val kieContainer: KieContainer) {

	private val log = LoggerFactory.getLogger(javaClass)

	fun validate(order: OrderMessage): OrderMessage {
		normalize(order)
		val session = kieContainer.newKieSession()
		try {
			session.insert(order)
			session.fireAllRules()
			order.applyValidationStatus()
			log.info(
				"Validated orderId={}, status={}, reasons={}",
				order.orderId,
				order.validationStatus,
				order.rejectionReasons
			)
			return order
		} finally {
			session.dispose()
		}
	}

	private fun normalize(order: OrderMessage) {
		order.orderId = order.orderId?.trim()
		order.customerName = order.customerName?.trim()
		order.country = order.country?.trim()?.toUpperCase(Locale.ROOT)
		order.orderType = order.orderType?.trim()?.toUpperCase(Locale.ROOT)
	}
}
