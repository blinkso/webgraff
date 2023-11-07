package ua.blink.whatsappgraff.exception

import ua.blink.whatsappgraff.dto.request.SendRequest

class CancelException(val messageRequest: SendRequest) : HandlerException("Handler cancelled")