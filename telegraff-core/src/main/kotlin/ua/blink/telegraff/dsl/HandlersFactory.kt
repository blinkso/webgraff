package ua.blink.telegraff.dsl

interface HandlersFactory {
    fun getHandlers(): Map<String, Handler>
}