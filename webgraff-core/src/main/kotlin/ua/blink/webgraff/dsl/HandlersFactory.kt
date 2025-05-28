package ua.blink.webgraff.dsl

interface HandlersFactory {
    fun getHandlers(): Map<String, Handler>
}