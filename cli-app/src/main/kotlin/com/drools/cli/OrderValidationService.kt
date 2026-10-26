package com.drools.cli

import org.kie.api.runtime.KieContainer
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class OrderValidationService(private val kieContainer: KieContainer) {

	fun validate(order: OrderMessage): OrderMessage {
		order.orderId = order.orderId?.trim()
		order.customerName = order.customerName?.trim()
		order.country = order.country?.trim()?.uppercase(Locale.ROOT)
		order.orderType = order.orderType?.trim()?.uppercase(Locale.ROOT)

		val session = kieContainer.newKieSession()
		try {
			session.insert(order)
			session.fireAllRules()
			order.applyValidationStatus()
			return order
		} finally {
			session.dispose()
		}
	}
}
