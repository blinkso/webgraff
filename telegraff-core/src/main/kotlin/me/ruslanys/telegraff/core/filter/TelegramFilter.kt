package me.ruslanys.telegraff.core.filter

import me.ruslanys.telegraff.core.dto.TelegramMessage

interface TelegramFilter {
    suspend fun handleMessage(message: TelegramMessage, chain: TelegramFilterChain)
}