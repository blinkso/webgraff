package ua.blink.whatsappgraff.dsl

import org.springframework.stereotype.Component
import ua.blink.whatsappgraff.dto.request.MessageSendRequest

@Component
class DefaultButtonsFactory : ButtonsFactory {

    // Store a list of button requests for each chatId
    private val buttonRequests: MutableMap<String, MutableList<MessageSendRequest>> = hashMapOf()

    override fun addButtonsRequest(sendRequest: MessageSendRequest) {
        buttonRequests.getOrPut(sendRequest.chatId) { mutableListOf() }.add(sendRequest)
    }

    // Get all button requests for a chatId
    override fun getButtonsRequests(chatId: String): List<MessageSendRequest> {
        return buttonRequests[chatId] ?: emptyList()
    }
}