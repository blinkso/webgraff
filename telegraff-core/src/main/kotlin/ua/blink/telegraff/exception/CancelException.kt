package ua.blink.telegraff.exception

import ua.blink.telegraff.dto.request.TelegramSendRequest

class CancelException(val messageRequest: TelegramSendRequest) : HandlerException("Handler cancelled")