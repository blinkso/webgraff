package me.ruslanys.telegraff.core.dto.request

import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramMarkupContactReplyKeyboard
import java.util.*

class ContactRequestMessage(text: String, locale: Locale?, chatId: Long = 0) :
    TelegramMessageSendRequest(
        chatId,
        text,
        TelegramParseMode.MARKDOWN,
        TelegramMarkupContactReplyKeyboard(text = text, locale = locale)
    )