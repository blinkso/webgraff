package ua.blink.whatsappgraff.dsl

import java.util.concurrent.ConcurrentHashMap

class HandlerState(
    val chatId: String,
    val handler: Handler
) {

    var currentStep: Step<*>? = handler.getInitialStep()
    val answers: MutableMap<String, Any> = ConcurrentHashMap()
    val attributes: MutableMap<String, Any> = ConcurrentHashMap()

}