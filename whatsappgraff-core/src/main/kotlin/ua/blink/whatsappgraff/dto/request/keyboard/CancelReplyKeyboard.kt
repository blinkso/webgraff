package ua.blink.whatsappgraff.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

class CancelReplyKeyboard(
    cancelButtonText: String,

    @get:JsonProperty("buttons")
    val buttons: List<InlineUrlReplyKeyboard> = listOf(
        InlineUrlReplyKeyboard(
            text = cancelButtonText,
            callbackData = cancelButtonText
        )
    )
) : ReplyKeyboard() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CancelReplyKeyboard

        if (buttons != other.buttons) return false

        return true
    }

    override fun hashCode(): Int {
        return buttons.hashCode()
    }
}