package ua.blink.webgraff.filter

import ua.blink.webgraff.dto.Message

interface Filter {
    suspend fun handleMessage(message: Message, chain: FilterChain)
}