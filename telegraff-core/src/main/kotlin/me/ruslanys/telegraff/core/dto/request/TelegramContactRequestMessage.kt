package me.ruslanys.telegraff.core.dto.request

import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramMarkupContactReplyKeyboard

open class TelegramContactRequestMessage(
    chatId: Long = 0,

    text: String,

    buttonText: String,

    cancelButtonText: String? = null
) : TelegramMessageSendRequest(
    chatId = chatId,
    text = text,
    parseMode = TelegramParseMode.MARKDOWN,
    replyMarkup = TelegramMarkupContactReplyKeyboard(text = buttonText, cancelButtonText = cancelButtonText)
)