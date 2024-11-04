package ua.blink.whatsappgraff.dsl

import ua.blink.whatsappgraff.dto.request.MessageSendRequest

interface ButtonsFactory {

    fun addButtonsRequest(sendRequest: MessageSendRequest)

    fun getButtonsRequests(chatId: String): List<MessageSendRequest>
}