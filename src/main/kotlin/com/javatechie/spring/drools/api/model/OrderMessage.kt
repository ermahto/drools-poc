package com.javatechie.spring.drools.api.model

data class OrderMessage(
	var orderId: String? = null,
	var customerName: String? = null,
	var country: String? = null,
	var orderType: String? = null,
	var amount: Double? = null,
	var valid: Boolean = true,
	var validationStatus: String = "PENDING",
	var rejectionReasons: MutableList<String> = mutableListOf()
) {
	fun reject(reason: String) {
		valid = false
		if (reason !in rejectionReasons) {
			rejectionReasons.add(reason)
		}
	}

	fun applyValidationStatus() {
		validationStatus = if (valid) "VALIDATED" else "REJECTED"
	}
}
