package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import ua.blink.telegraff.dto.request.keyboard.TelegramRemoveReplyKeyboard

abstract class TelegramMediaSendRequest(
    chatId: Long,

    @get:JsonProperty("caption")
    val caption: String? = null,

    @get:JsonProperty("parse_mode")
    val parseMode: TelegramParseMode? = null
) : TelegramSendRequest(chatId, TelegramRemoveReplyKeyboard())