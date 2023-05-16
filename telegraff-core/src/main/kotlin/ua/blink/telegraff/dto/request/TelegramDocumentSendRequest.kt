package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

open class TelegramDocumentSendRequest(
    chatId: Long,

    @get:JsonProperty("document")
    val document: ByteArray,

    // Example: text.xls
    val name: String,

    caption: String? = null,

    parseMode: TelegramParseMode? = null
) : TelegramMediaSendRequest(chatId, caption, parseMode)