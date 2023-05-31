package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

open class TelegramVoiceSendRequest(
    chatId: Long,

    @get:JsonProperty("voice")
    val voice: ByteArray,

    caption: String? = null,

    parseMode: TelegramParseMode? = null
) : TelegramMediaSendRequest(chatId, caption, parseMode) {

    override fun toString(): String {
        return "TelegramVoiceSendRequest(voice=${voice.contentToString()})"
    }
}