package ua.blink.telegraff.dto.request

import ua.blink.telegraff.dto.request.keyboard.TelegramMarkupReplyKeyboard
import ua.blink.telegraff.dto.request.keyboard.TelegramRemoveReplyKeyboard

class MarkdownMessage(
    text: String,
    vararg replies: String,
    cancelButtonText: String? = null,
    chatId: Long = 0
) : TelegramMessageSendRequest(
    chatId,
    text,
    TelegramParseMode.MARKDOWN,
    if (replies.isNotEmpty()) {
        TelegramMarkupReplyKeyboard(
            answers = replies.asList(),
            cancelButtonText = cancelButtonText
        )
    } else {
        TelegramRemoveReplyKeyboard()
    }
)