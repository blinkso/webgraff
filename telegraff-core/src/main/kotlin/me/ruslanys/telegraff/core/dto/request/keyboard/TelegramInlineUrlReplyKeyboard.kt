package me.ruslanys.telegraff.core.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty
import me.ruslanys.telegraff.core.util.EMPTY

class TelegramInlineUrlReplyKeyboard(
    @get:JsonProperty("text")
    val text: String,
    @get:JsonProperty("url")
    val url: String = String.EMPTY,
    @get:JsonProperty("callback_data")
    val callbackData: String = String.EMPTY
) : TelegramReplyKeyboard() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TelegramInlineUrlReplyKeyboard

        if (text != other.text) return false
        if (url != other.url) return false
        if (callbackData != other.callbackData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + callbackData.hashCode()
        return result
    }
}