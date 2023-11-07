package ua.blink.whatsappgraff.component

import ua.blink.whatsappgraff.dto.Message
import ua.blink.whatsappgraff.dto.request.DocumentSendRequest
import ua.blink.whatsappgraff.dto.request.MessageSendRequest
import ua.blink.whatsappgraff.dto.request.PhotoSendRequest
import ua.blink.whatsappgraff.dto.request.VoiceSendRequest

interface ConversationApi {

    fun getUpdates(offset: String?, timeout: Long?): List<Message>

    fun getUpdates(offset: String?): List<Message> {
        return getUpdates(offset, null)
    }

    fun getUpdates(): List<Message> {
        return getUpdates(null)
    }

    fun getFileByPath(filePath: String): ByteArray

    fun setWebhook(url: String)

    fun removeWebhook()

    fun sendMessage(request: MessageSendRequest): Message

    fun sendPhoto(request: PhotoSendRequest): Message

    fun sendDocument(request: DocumentSendRequest): Message

    fun sendVoice(request: VoiceSendRequest): Message
}
