package ua.blink.whatsappgraff.event

import org.springframework.context.ApplicationEvent
import ua.blink.whatsappgraff.dto.Message

class UpdateEvent(
    source: Any,
    val update: Message?
) : ApplicationEvent(source)