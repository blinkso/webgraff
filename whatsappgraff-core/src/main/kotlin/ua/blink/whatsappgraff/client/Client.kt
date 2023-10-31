package ua.blink.whatsappgraff.client

import ua.blink.whatsappgraff.dto.Message

interface Client {
    fun start()

    fun shutdown()

    fun onUpdate(update: Message)
}