package me.ruslanys.telegraff.core.filter

import kotlinx.coroutines.CoroutineScope
import me.ruslanys.telegraff.core.dto.TelegramMessage

interface TelegramFilterChain {
    suspend fun doFilter(message: TelegramMessage, lifecycleScope: CoroutineScope)
}