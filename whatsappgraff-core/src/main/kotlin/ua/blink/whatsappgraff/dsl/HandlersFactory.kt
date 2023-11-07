package ua.blink.whatsappgraff.dsl

interface HandlersFactory {
    fun getHandlers(): Map<String, Handler>
}