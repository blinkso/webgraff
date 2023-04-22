package ua.blink.telegraff.exception

open class HandlerException(override val message: String) : RuntimeException(message)