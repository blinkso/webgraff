package ua.blink.whatsappgraff.filter

import ua.blink.whatsappgraff.dto.Message

interface Filter {
    suspend fun handleMessage(message: Message, chain: FilterChain)
}