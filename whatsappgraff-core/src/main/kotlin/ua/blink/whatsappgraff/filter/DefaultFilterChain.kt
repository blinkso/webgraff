package ua.blink.whatsappgraff.filter

import ua.blink.whatsappgraff.dto.Message


class DefaultFilterChain(filters: List<Filter>) : FilterChain {

    private val iterator: Iterator<Filter> = filters.iterator()

    override suspend fun doFilter(message: Message) {
        if (iterator.hasNext()) {
            val filter = iterator.next()
            filter.handleMessage(message, this)
        }
    }

}