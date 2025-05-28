package ua.blink.webgraff.exception

import ua.blink.webgraff.dto.request.SendRequest

class CancelException(val messageRequest: SendRequest) : HandlerException("Handler cancelled")