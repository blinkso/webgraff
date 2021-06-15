package me.ruslanys.telegraff.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TelegramMessage(
    @JsonProperty("message_id")
    val id: Long,

    @JsonProperty("from")
    val user: TelegramUser?,

    @JsonProperty("date")
    val date: Long,

    @JsonProperty("chat")
    val chat: TelegramChat,

    @JsonProperty("contact")
    val contact: TelegramContact?,

    @JsonProperty("text")
    var text: String?,

    // Used for callback populating and filtering
    @JsonProperty("callback_query")
    var callbackQuery: TelegramCallbackQuery? = null
)