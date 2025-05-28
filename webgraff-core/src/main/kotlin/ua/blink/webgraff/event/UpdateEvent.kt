package ua.blink.webgraff.event

import org.springframework.context.ApplicationEvent
import ua.blink.webgraff.dto.Message

class UpdateEvent(
    source: Any,
    val update: Message?
) : ApplicationEvent(source)