package ua.blink.webgraff.dsl

import ua.blink.webgraff.dto.request.MessageSendRequest

interface ButtonsFactory {

    fun addButtonsRequest(sendRequest: MessageSendRequest)

    fun getButtonsRequests(chatId: String): List<MessageSendRequest>
}