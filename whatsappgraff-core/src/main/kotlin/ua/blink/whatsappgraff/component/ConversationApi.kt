package ua.blink.whatsappgraff.component

import ua.blink.whatsappgraff.dto.File
import ua.blink.whatsappgraff.dto.Message
import ua.blink.whatsappgraff.dto.Update
import ua.blink.whatsappgraff.dto.request.DocumentSendRequest
import ua.blink.whatsappgraff.dto.request.MessageSendRequest
import ua.blink.whatsappgraff.dto.request.PhotoSendRequest
import ua.blink.whatsappgraff.dto.request.VoiceSendRequest

interface ConversationApi {

    fun getUpdates(offset: Long?, timeout: Int?): List<Update>

    fun getUpdates(offset: Long?): List<Update> {
        return getUpdates(offset, null)
    }

    fun getUpdates(): List<Update> {
        return getUpdates(null)
    }

    fun getFile(fileId: String): File

    fun getFileByPath(filePath: String): ByteArray

    fun setWebhook(url: String): Boolean

    fun removeWebhook(): Boolean

    fun sendMessage(request: MessageSendRequest): Message

    fun sendPhoto(request: PhotoSendRequest): Message

    fun sendDocument(request: DocumentSendRequest): Message

    fun sendVoice(request: VoiceSendRequest): Message
}
