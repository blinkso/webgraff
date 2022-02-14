package me.ruslanys.telegraff.core.dto.request

import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramMarkupReplyKeyboard
import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramRemoveReplyKeyboard

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