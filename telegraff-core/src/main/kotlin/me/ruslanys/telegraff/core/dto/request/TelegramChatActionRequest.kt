package me.ruslanys.telegraff.core.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramRemoveReplyKeyboard

open class TelegramChatActionRequest(
    chatId: Long = 0,

    @get:JsonProperty("action")
    val action: TelegramChatAction
) : TelegramSendRequest(chatId, TelegramRemoveReplyKeyboard())
