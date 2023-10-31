package ua.blink.whatsappgraff.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

class CancelReplyKeyboard(
    cancelButtonText: String,
    @get:JsonProperty("text")
    val text: String = cancelButtonText
) : ReplyKeyboard() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CancelReplyKeyboard

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }
}