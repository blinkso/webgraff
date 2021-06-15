package me.ruslanys.telegraff.core.filter

import me.ruslanys.telegraff.core.annotation.TelegramFilterOrder
import me.ruslanys.telegraff.core.component.TelegramApi
import me.ruslanys.telegraff.core.dto.TelegramMessage
import me.ruslanys.telegraff.core.filter.FilterOrders.Companion.CALLBACK_QUERY_ANSWER_FILTER_ORDER

@TelegramFilterOrder(CALLBACK_QUERY_ANSWER_FILTER_ORDER)
class CallbackQueryAnswerFilter(
    private val telegramApi: TelegramApi
) : TelegramFilter {

    override fun handleMessage(message: TelegramMessage, chain: TelegramFilterChain) {
        if (message.callbackQuery != null) {
            // Sending callback query answer to telegram api
            telegramApi.sendAnswerCallbackQuery(message.callbackQuery?.id ?: 0L)
            chain.doFilter(message)
        } else {
            chain.doFilter(message)
        }
    }
}