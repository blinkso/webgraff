package me.ruslanys.telegraff.core.util

import me.ruslanys.telegraff.core.component.TelegramApi

val String.Companion.EMPTY: String
    get() = ""

val String.Companion.SPACE: String
    get() = " "

fun Any.anyToLong(): Long {
    return this.toString().toLong()
}

fun TelegramApi.getCurrentChatId(): Long? {
    return getUpdates().getOrNull(0)?.message?.chat?.id
}

fun String.readClasspathResource(): ByteArray {
    return javaClass.classLoader.getResourceAsStream(this).use {
        it.readBytes()
    }
}