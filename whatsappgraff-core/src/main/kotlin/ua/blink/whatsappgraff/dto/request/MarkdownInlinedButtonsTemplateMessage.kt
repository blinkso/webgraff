package ua.blink.whatsappgraff.dto.request

import ua.blink.whatsappgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard

class MarkdownInlinedButtonsTemplateMessage(
    val contentSid: String,
    vararg variables: String,
    chatId: String = "",
    to: String = ""
) : MessageSendRequest(
    chatId = chatId,
    to = to,
    text = contentSid,
    replyMarkup = if (variables.isNotEmpty()) {
        val inlines = variables.map { reply ->
            InlineUrlReplyKeyboard(
                text = reply,
                url = reply
            )
        }
        MarkupInlinedReplyKeyboard(
            inlines = inlines
        )
    } else {
        null
    }
)