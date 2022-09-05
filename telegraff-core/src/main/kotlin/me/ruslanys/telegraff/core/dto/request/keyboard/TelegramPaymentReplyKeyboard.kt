package me.ruslanys.telegraff.core.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

class TelegramPaymentReplyKeyboard(
    @get:JsonProperty("text")
    val text: String,

    @get:JsonProperty("pay")
    val pay: Boolean = true
) : TelegramReplyKeyboard() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TelegramPaymentReplyKeyboard) return false
        if (!super.equals(other)) return false

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }
}