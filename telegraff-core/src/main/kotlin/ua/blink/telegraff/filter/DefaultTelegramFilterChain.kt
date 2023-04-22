package ua.blink.telegraff.filter

import ua.blink.telegraff.dto.TelegramMessage


class DefaultTelegramFilterChain(filters: List<TelegramFilter>) : TelegramFilterChain {

    private val iterator: Iterator<TelegramFilter> = filters.iterator()

    override suspend fun doFilter(message: TelegramMessage) {
        if (iterator.hasNext()) {
            val filter = iterator.next()
            filter.handleMessage(message, this)
        }
    }

}