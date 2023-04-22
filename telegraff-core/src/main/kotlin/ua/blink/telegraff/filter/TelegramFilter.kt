package ua.blink.telegraff.filter

import ua.blink.telegraff.dto.TelegramMessage

interface TelegramFilter {
    suspend fun handleMessage(message: TelegramMessage, chain: TelegramFilterChain)
}