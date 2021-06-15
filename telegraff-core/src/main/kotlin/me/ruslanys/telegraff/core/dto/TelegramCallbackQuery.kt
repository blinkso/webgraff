package me.ruslanys.telegraff.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TelegramCallbackQuery(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("from")
    val user: TelegramUser,

    @JsonProperty("message")
    val message: TelegramMessage?,

    @JsonProperty("inline_message_id")
    val inlineMessageId: String?,

    @JsonProperty("chat_instance")
    val chatInstance: Long,

    @JsonProperty("data")
    val data: String?
)