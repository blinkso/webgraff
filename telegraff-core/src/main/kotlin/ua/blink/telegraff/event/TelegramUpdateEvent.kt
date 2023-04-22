package ua.blink.telegraff.event

import org.springframework.context.ApplicationEvent
import ua.blink.telegraff.dto.TelegramUpdate

class TelegramUpdateEvent(
    source: Any,
    val update: TelegramUpdate
) : ApplicationEvent(source)