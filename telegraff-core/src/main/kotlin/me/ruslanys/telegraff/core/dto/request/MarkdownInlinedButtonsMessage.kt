package me.ruslanys.telegraff.core.dto.request

import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramInlineUrlReplyKeyboard
import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramMarkupInlinedReplyKeyboard
import me.ruslanys.telegraff.core.dto.request.keyboard.TelegramRemoveReplyKeyboard

class MarkdownInlinedButtonsMessage(text: String, vararg inlines: TelegramInlineUrlReplyKeyboard, chatId: Long = 0) :
    TelegramMessageSendRequest(
        chatId,
        text,
        TelegramParseMode.MARKDOWN,
        if (inlines.isNotEmpty()) TelegramMarkupInlinedReplyKeyboard(
            inlines = inlines.toList()
        ) else TelegramRemoveReplyKeyboard()
    )