package me.ruslanys.telegraff.core.dto.request

import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramMarkupContactReplyKeyboard

class TelegramContactRequestMessage(
    text: String,
    buttonText: String,
    cancelButtonText: String? = null,
    chatId: Long = 0
) : TelegramMessageSendRequest(
    chatId,
    text,
    TelegramParseMode.MARKDOWN,
    TelegramMarkupContactReplyKeyboard(text = buttonText, cancelButtonText = cancelButtonText)
)