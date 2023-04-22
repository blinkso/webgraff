package ua.blink.telegraff.dto.request

import ua.blink.telegraff.dto.request.keyboard.TelegramMarkupReplyKeyboard
import ua.blink.telegraff.dto.request.keyboard.TelegramRemoveReplyKeyboard

class HtmlMessage(text: String, vararg replies: String, chatId: Long = 0) : TelegramMessageSendRequest(
    chatId,
    text,
    TelegramParseMode.HTML,
    if (replies.isNotEmpty()) TelegramMarkupReplyKeyboard(
        answers = replies.asList()
    ) else TelegramRemoveReplyKeyboard()
)