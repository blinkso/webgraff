package ua.blink.telegraff.filter

import org.slf4j.LoggerFactory
import ua.blink.telegraff.component.TelegramApi
import ua.blink.telegraff.dto.TelegramMessage
import ua.blink.telegraff.filter.FilterOrders.Companion.CALLBACK_QUERY_ANSWER_FILTER_ORDER

@ua.blink.telegraff.annotation.TelegramFilterOrder(CALLBACK_QUERY_ANSWER_FILTER_ORDER)
class CallbackQueryAnswerFilter(
    private val telegramApi: TelegramApi
) : TelegramFilter {

    override suspend fun handleMessage(
        message: TelegramMessage,
        chain: TelegramFilterChain
    ) {
        if (message.callbackQuery != null) {
            // Sending callback query answer to telegram api
            try {
                telegramApi.sendAnswerCallbackQuery(message.callbackQuery?.id ?: 0L)
            } catch (e: Throwable) {
                log.error("Error sending callback query answer: ${e.printStackTrace()}")
            }
            chain.doFilter(message)
        } else {
            chain.doFilter(message)
        }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(CallbackQueryAnswerFilter::class.java)
    }
}