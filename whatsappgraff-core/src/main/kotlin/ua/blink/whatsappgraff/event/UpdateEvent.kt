package ua.blink.whatsappgraff.event

import org.springframework.context.ApplicationEvent
import ua.blink.whatsappgraff.dto.Update

class UpdateEvent(
    source: Any,
    val update: Update
) : ApplicationEvent(source)