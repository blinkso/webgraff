package ua.blink.webgraff.filter

import ua.blink.webgraff.dto.Message

interface FilterChain {
    suspend fun doFilter(message: Message)
}