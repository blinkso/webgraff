package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

// https://core.telegram.org/bots/api#labeledprice
open class TelegramLabeledPrice(
    @get:JsonProperty("label")
    val label: String,

    @get:JsonProperty("amount")
    val amount: Int,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TelegramLabeledPrice) return false

        if (label != other.label) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + amount
        return result
    }
}