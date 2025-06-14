package ua.blink.webgraff.component

import ua.blink.webgraff.dto.Conversation
import ua.blink.webgraff.dto.Message
import ua.blink.webgraff.dto.request.DocumentSendRequest
import ua.blink.webgraff.dto.request.MessageSendRequest
import ua.blink.webgraff.dto.request.PhotoSendRequest
import ua.blink.webgraff.dto.request.VoiceSendRequest

interface ConversationApi {

    fun getUpdates(offset: String?, timeout: Long?): List<Message>

    fun getUpdates(offset: String?): List<Message> {
        return getUpdates(offset, null)
    }

    fun getUpdates(): List<Message> {
        return getUpdates(null)
    }

    fun getMessages(chatId: String): List<Message>

    fun getFileByPath(filePath: String): ByteArray

    fun setWebhook(url: String)

    fun removeWebhook()

    fun sendMessage(request: MessageSendRequest): Message

    fun sendPhoto(request: PhotoSendRequest): Message

    fun sendDocument(request: DocumentSendRequest): Message

    fun sendVoice(request: VoiceSendRequest): Message
    
    /**
     * Creates a new conversation for web chat.
     * 
     * @param identity The identity of the user
     * @return The created conversation
     */
    fun createConversation(identity: String): Conversation
}
