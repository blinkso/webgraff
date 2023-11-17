package ua.blink.whatsappgraff.dsl

import ua.blink.whatsappgraff.dto.request.MessageSendRequest

interface ButtonsFactory {

    fun addButtonsRequest(sendRequest: MessageSendRequest)

    fun getButtonsRequest(chatId: String): MessageSendRequest?

}