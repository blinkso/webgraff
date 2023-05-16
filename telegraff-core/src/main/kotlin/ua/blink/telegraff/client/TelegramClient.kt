package ua.blink.telegraff.client

import ua.blink.telegraff.dto.TelegramUpdate

interface TelegramClient {
    fun start()

    fun shutdown()

    fun onUpdate(update: TelegramUpdate)
}