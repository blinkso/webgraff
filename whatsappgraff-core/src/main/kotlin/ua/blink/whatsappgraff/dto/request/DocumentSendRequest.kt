package ua.blink.whatsappgraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

open class DocumentSendRequest(
    chatId: Long,

    @get:JsonProperty("document")
    val document: ByteArray,

    // Example: text.xls
    val name: String,

    caption: String? = null
) : MediaSendRequest(chatId, caption) {

    override fun toString(): String {
        return "DocumentSendRequest(document=${document.contentToString()}, name='$name')"
    }
}