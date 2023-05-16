package ua.blink.telegraff.filter

import org.springframework.context.event.EventListener
import ua.blink.telegraff.event.TelegramUpdateEvent

interface TelegramFilterProcessor {
    @EventListener(TelegramUpdateEvent::class)
    fun process(event: TelegramUpdateEvent)
}