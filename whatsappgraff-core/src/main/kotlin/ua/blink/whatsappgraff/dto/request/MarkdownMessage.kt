package ua.blink.whatsappgraff.dto.request

import ua.blink.whatsappgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard

class MarkdownMessage(
    text: String,
    vararg replies: String,
    chooseActionButton: String? = null,
    chatId: String = ""
) : MessageSendRequest(
    chatId,
    text,
    if (replies.isNotEmpty()) {
        val inlines = replies.map { reply ->
            InlineUrlReplyKeyboard(
                text = reply,
                callbackData = reply
            )
        }
        MarkupInlinedReplyKeyboard(
            inlines = inlines,
            chooseActionButton = chooseActionButton
        )
    } else {
        null
    }
)