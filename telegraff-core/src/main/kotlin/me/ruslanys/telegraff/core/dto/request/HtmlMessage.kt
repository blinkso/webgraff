package me.ruslanys.telegraff.core.dto.request

import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramMarkupReplyKeyboard
import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramRemoveReplyKeyboard
import java.util.*

class HtmlMessage(text: String, vararg replies: String, locale: Locale?, chatId: Long = 0) : TelegramMessageSendRequest(
    chatId,
    text,
    TelegramParseMode.HTML,
    if (replies.isNotEmpty()) TelegramMarkupReplyKeyboard(
        answers = replies.asList(),
        locale = locale
    ) else TelegramRemoveReplyKeyboard()
)