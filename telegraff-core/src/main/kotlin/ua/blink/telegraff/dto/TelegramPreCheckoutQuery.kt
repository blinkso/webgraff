package ua.blink.telegraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TelegramPreCheckoutQuery(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("from")
    val user: TelegramUser,

    @JsonProperty("currency")
    val currency: String,

    @JsonProperty("total_amount")
    val totalAmount: Int,

    @JsonProperty("invoice_payload")
    val invoicePayload: String,
)