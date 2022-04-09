package me.ruslanys.telegraff.core.exception

import me.ruslanys.telegraff.core.dto.request.TelegramSendRequest

class CancelException(val messageRequest: TelegramSendRequest) : HandlerException("Handler cancelled") {
}