package ua.blink.whatsappgraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import ua.blink.whatsappgraff.dto.request.keyboard.RemoveReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.ReplyKeyboard

open class MessageSendRequest(
    chatId: String,

    @get:JsonProperty("Body")
    val text: String,

    replyMarkup: ReplyKeyboard = RemoveReplyKeyboard(),
) : SendRequest(chatId, replyMarkup) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageSendRequest) return false
        if (!super.equals(other)) return false

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }

    override fun toString(): String {
        return "MessageSendRequest(text='$text')"
    }
}