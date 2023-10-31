package ua.blink.whatsappgraff.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import ua.blink.whatsappgraff.dto.request.keyboard.ReplyKeyboard

@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class SendRequest(
    @get:JsonProperty("chat_id")
    var chatId: Long,

    @get:JsonProperty("reply_markup")
    val replyKeyboard: ReplyKeyboard,

    @get:JsonProperty("disable_notification")
    val disableNotification: Boolean = false
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SendRequest) return false

        if (chatId != other.chatId) return false
        if (replyKeyboard != other.replyKeyboard) return false
        if (disableNotification != other.disableNotification) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chatId.hashCode()
        result = 31 * result + replyKeyboard.hashCode()
        result = 31 * result + disableNotification.hashCode()
        return result
    }

}