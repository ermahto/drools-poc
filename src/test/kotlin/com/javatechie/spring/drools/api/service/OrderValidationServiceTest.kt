package com.javatechie.spring.drools.api.service

import com.javatechie.spring.drools.api.config.DroolsContainerFactory
import com.javatechie.spring.drools.api.model.OrderMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class OrderValidationServiceTest {

	private lateinit var service: OrderValidationService

	@Before
	fun setUp() {
		service = OrderValidationService(DroolsContainerFactory.create())
	}

	@Test
	fun validStandardOrderIsAccepted() {
		val result = service.validate(validOrder())

		assertThat(result.valid).isTrue()
		assertThat(result.validationStatus).isEqualTo("VALIDATED")
		assertThat(result.rejectionReasons).isEmpty()
	}

	@Test
	fun validExpressAndBulkOrdersAreAccepted() {
		val express = service.validate(validOrder("ORD-EXPRESS", "EXPRESS", 25000.0, "IN"))
		val bulk = service.validate(validOrder("ORD-BULK", "BULK", 80000.0, "DE"))

		assertThat(express.valid).isTrue()
		assertThat(bulk.valid).isTrue()
	}

	@Test
	fun missingMandatoryFieldsAreRejected() {
		val result = service.validate(OrderMessage(country = "US", orderType = "STANDARD"))

		assertThat(result.valid).isFalse()
		assertThat(result.validationStatus).isEqualTo("REJECTED")
		assertThat(result.rejectionReasons).contains(
			"orderId is mandatory",
			"customerName is mandatory",
			"amount is mandatory"
		)
	}

	@Test
	fun unsupportedCountryIsRejected() {
		val result = service.validate(validOrder(country = "FR"))

		assertThat(result.valid).isFalse()
		assertThat(result.rejectionReasons).anyMatch { it.contains("country 'FR' is not supported") }
	}

	@Test
	fun zeroAmountIsRejected() {
		val result = service.validate(validOrder(amount = 0.0))

		assertThat(result.valid).isFalse()
		assertThat(result.rejectionReasons).contains("amount must be greater than 0")
	}

	@Test
	fun standardOrderAboveLimitIsRejected() {
		val result = service.validate(validOrder(orderType = "STANDARD", amount = 15000.0))

		assertThat(result.valid).isFalse()
		assertThat(result.rejectionReasons).contains("STANDARD orders cannot exceed amount 10000")
	}

	@Test
	fun unknownOrderTypeIsRejected() {
		val result = service.validate(validOrder(orderType = "PRIORITY"))

		assertThat(result.valid).isFalse()
		assertThat(result.rejectionReasons).anyMatch { it.contains("orderType 'PRIORITY' is not supported") }
	}

	@Test
	fun multipleRuleViolationsAreCollected() {
		val result = service.validate(
			OrderMessage(
				orderId = "ORD-BAD",
				customerName = "Alex",
				country = "XX",
				orderType = "STANDARD",
				amount = 20000.0
			)
		)

		assertThat(result.valid).isFalse()
		assertThat(result.rejectionReasons).hasSize(2)
		assertThat(result.rejectionReasons).anyMatch { it.contains("country") }
		assertThat(result.rejectionReasons).anyMatch { it.contains("STANDARD orders cannot exceed") }
	}

	private fun validOrder(
		orderId: String = "ORD-1001",
		orderType: String = "STANDARD",
		amount: Double = 5000.0,
		country: String = "US"
	) = OrderMessage(
		orderId = orderId,
		customerName = "Alex Kumar",
		country = country,
		orderType = orderType,
		amount = amount
	)
}
