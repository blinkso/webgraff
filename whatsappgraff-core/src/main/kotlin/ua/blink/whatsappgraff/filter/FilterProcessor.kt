package ua.blink.whatsappgraff.filter

import org.springframework.context.event.EventListener
import ua.blink.whatsappgraff.event.UpdateEvent

interface FilterProcessor {
    @EventListener(UpdateEvent::class)
    fun process(event: UpdateEvent)
}