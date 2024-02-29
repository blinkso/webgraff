package ua.blink.whatsappgraff.dto.request

import ua.blink.whatsappgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard

class MarkdownTemplateMessage(
    val contentSid: String,
    vararg variables: String,
    chatId: String = ""
) : MessageSendRequest(
    chatId,
    contentSid,
    if (variables.isNotEmpty()) {
        val inlines = variables.map { reply ->
            InlineUrlReplyKeyboard(
                text = reply,
                callbackData = reply
            )
        }
        MarkupInlinedReplyKeyboard(
            inlines = inlines
        )
    } else {
        null
    }
)