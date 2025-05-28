package ua.blink.webgraff.client

import ua.blink.webgraff.dto.Message

interface Client {
    fun start()

    fun shutdown()

    fun onUpdate(update: Message)
}