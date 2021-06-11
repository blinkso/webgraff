package me.ruslanys.telegraff.core.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

class TelegramContactReplyKeyboard(
    @get:JsonProperty("text")
    val text: String,
    @get:JsonProperty("request_contact")
    val requestContact: Boolean = true
) : TelegramReplyKeyboard() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TelegramContactReplyKeyboard

        if (text != other.text) return false
        if (requestContact != other.requestContact) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + requestContact.hashCode()
        return result
    }
}