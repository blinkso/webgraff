package ua.blink.webgraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

open class DocumentSendRequest(
    chatId: String,

    to: String,

    @get:JsonProperty("document")
    val document: ByteArray,

    // Example: text.xls
    val name: String,

    caption: String? = null
) : MediaSendRequest(chatId = chatId, to = to, caption = caption) {

    override fun toString(): String {
        return "DocumentSendRequest(document=${document.contentToString()}, name='$name')"
    }
}