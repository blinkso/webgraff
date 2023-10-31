package ua.blink.whatsappgraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import ua.blink.whatsappgraff.dto.request.keyboard.RemoveReplyKeyboard

abstract class MediaSendRequest(
    chatId: Long,

    @get:JsonProperty("caption")
    val caption: String? = null,
) : SendRequest(chatId, RemoveReplyKeyboard())