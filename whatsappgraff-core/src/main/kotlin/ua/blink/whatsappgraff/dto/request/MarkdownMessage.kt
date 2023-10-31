package ua.blink.whatsappgraff.dto.request

import ua.blink.whatsappgraff.dto.request.keyboard.MarkupReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.RemoveReplyKeyboard

class MarkdownMessage(
    text: String,
    vararg replies: String,
    cancelButtonText: String? = null,
    chatId: Long = 0
) : MessageSendRequest(
    chatId,
    text,
    if (replies.isNotEmpty()) {
        MarkupReplyKeyboard(
            answers = replies.asList(),
            cancelButtonText = cancelButtonText
        )
    } else {
        RemoveReplyKeyboard()
    }
)