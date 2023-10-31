package ua.blink.whatsappgraff.client

import ua.blink.whatsappgraff.dto.Update

interface Client {
    fun start()

    fun shutdown()

    fun onUpdate(update: Update)
}