package ua.blink.whatsappgraff.dsl

import ua.blink.whatsappgraff.dto.Chat
import java.util.concurrent.ConcurrentHashMap

class HandlerState(
    val chat: Chat,
    val handler: Handler
) {

    var currentStep: Step<*>? = handler.getInitialStep()
    val answers: MutableMap<String, Any> = ConcurrentHashMap()
    val attributes: MutableMap<String, Any> = ConcurrentHashMap()

}