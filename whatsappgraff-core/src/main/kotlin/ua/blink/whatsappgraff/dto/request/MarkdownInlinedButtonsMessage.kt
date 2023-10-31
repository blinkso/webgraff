package ua.blink.whatsappgraff.dto.request

import ua.blink.whatsappgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.RemoveReplyKeyboard

class MarkdownInlinedButtonsMessage(text: String, vararg inlines: InlineUrlReplyKeyboard, chatId: Long = 0) :
    MessageSendRequest(
        chatId,
        text,
        if (inlines.isNotEmpty()) MarkupInlinedReplyKeyboard(
            inlines = inlines.toList()
        ) else RemoveReplyKeyboard()
    )