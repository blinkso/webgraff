package ua.blink.whatsappgraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Conversation(
    @JsonProperty("sid")
    val chatId: String
)