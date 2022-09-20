package me.ruslanys.telegraff.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TelegramSuccessfulPayment(
    @JsonProperty("currency")
    val currency: String,

    @JsonProperty("total_amount")
    val totalAmount: Int,

    @JsonProperty("invoice_payload")
    val invoicePayload: String,

    @JsonProperty("telegram_payment_charge_id")
    val telegramPaymentChargeId: String,

    @JsonProperty("provider_payment_charge_id")
    val providerPaymentChargeId: String,
)