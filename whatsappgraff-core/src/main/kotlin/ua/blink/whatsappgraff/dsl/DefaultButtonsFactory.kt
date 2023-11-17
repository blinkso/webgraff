package ua.blink.whatsappgraff.dsl

import org.springframework.stereotype.Component
import ua.blink.whatsappgraff.dto.request.MessageSendRequest

@Component
class DefaultButtonsFactory : ButtonsFactory {

    private val buttonRequests: MutableMap<String, MessageSendRequest> = hashMapOf()

    override fun addButtonsRequest(sendRequest: MessageSendRequest) {
        buttonRequests[sendRequest.chatId] = sendRequest
    }

    override fun getButtonsRequest(chatId: String): MessageSendRequest? {
        return buttonRequests[chatId]
    }
}