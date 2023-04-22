package ua.blink.telegraff.filter

import ua.blink.telegraff.dto.TelegramMessage

interface TelegramFilterChain {
    suspend fun doFilter(message: TelegramMessage)
}