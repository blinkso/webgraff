package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import ua.blink.telegraff.dto.request.keyboard.TelegramRemoveReplyKeyboard

open class TelegramChatActionRequest(
    chatId: Long = 0,

    @get:JsonProperty("action")
    val action: TelegramChatAction
) : TelegramSendRequest(chatId, TelegramRemoveReplyKeyboard())
