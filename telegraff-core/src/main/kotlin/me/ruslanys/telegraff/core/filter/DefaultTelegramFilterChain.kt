package me.ruslanys.telegraff.core.filter

import kotlinx.coroutines.CoroutineScope
import me.ruslanys.telegraff.core.dto.TelegramMessage


class DefaultTelegramFilterChain(filters: List<TelegramFilter>) : TelegramFilterChain {

    private val iterator: Iterator<TelegramFilter> = filters.iterator()

    override suspend fun doFilter(message: TelegramMessage, lifecycleScope: CoroutineScope) {
        if (iterator.hasNext()) {
            val filter = iterator.next()
            filter.handleMessage(message, this, lifecycleScope)
        }
    }

}