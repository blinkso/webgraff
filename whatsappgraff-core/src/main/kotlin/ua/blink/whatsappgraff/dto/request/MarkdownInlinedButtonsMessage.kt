package ua.blink.whatsappgraff.dto.request

import ua.blink.whatsappgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard

class MarkdownInlinedButtonsMessage(text: String, vararg inlines: InlineUrlReplyKeyboard, chatId: String = "") :
    MessageSendRequest(
        chatId,
        text,
        if (inlines.isNotEmpty()) MarkupInlinedReplyKeyboard(
            inlines = inlines.toList()
        ) else null
    )