package com.drools.cli

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DroolsCliApplication(
	private val validationService: OrderValidationService
) : CommandLineRunner {

	override fun run(vararg args: String) {
		if (args.size >= 5) {
			printResult(
				validationService.validate(
					OrderMessage(args[0], args[1], args[2], args[3], args[4].toDouble())
				)
			)
			return
		}

		println("Drools CLI — evaluating sample orders")
		println()
		printResult(validationService.validate(OrderMessage("ORD-1001", "Alex Kumar", "US", "STANDARD", 5000.0)))
		printResult(validationService.validate(OrderMessage("ORD-BAD", "Alex Kumar", "FR", "STANDARD", 15000.0)))
	}

	private fun printResult(order: OrderMessage) {
		println("orderId  : ${order.orderId}")
		println("status   : ${order.validationStatus}")
		if (order.rejectionReasons.isNotEmpty()) {
			println("reasons  :")
			order.rejectionReasons.forEach { println("  - $it") }
		}
		println("----------------------------------------")
	}
}

fun main(args: Array<String>) {
	runApplication<DroolsCliApplication>(*args)
}
