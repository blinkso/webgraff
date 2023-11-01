package ua.blink.whatsappgraff.dto.request

import ua.blink.whatsappgraff.dto.request.keyboard.MarkupReplyKeyboard

class MarkdownMessage(
    text: String,
    vararg replies: String,
    cancelButtonText: String? = null,
    chatId: String = ""
) : MessageSendRequest(
    chatId,
    text,
    if (replies.isNotEmpty()) {
        MarkupReplyKeyboard(
            answers = replies.asList(),
            cancelButtonText = cancelButtonText
        )
    } else {
        null
    }
)