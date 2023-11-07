package ua.blink.whatsappgraff.filter

import ua.blink.whatsappgraff.dto.Message

interface FilterChain {
    suspend fun doFilter(message: Message)
}