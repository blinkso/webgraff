package ua.blink.webgraff.filter

import org.springframework.context.event.EventListener
import ua.blink.webgraff.event.UpdateEvent

interface FilterProcessor {
    @EventListener(UpdateEvent::class)
    fun process(event: UpdateEvent)
}