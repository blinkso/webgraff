package ua.blink.whatsappgraff.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

abstract class ReplyKeyboard(
    @get:JsonProperty("selective")
    val selective: Boolean = false
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReplyKeyboard) return false

        if (selective != other.selective) return false

        return true
    }

    override fun hashCode(): Int {
        return selective.hashCode()
    }

}