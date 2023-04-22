package ua.blink.telegraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TelegramUser(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("first_name")
    val firstName: String,

    @JsonProperty("last_name")
    val lastName: String?,

    @JsonProperty("username")
    val username: String?,

    @JsonProperty("language_code")
    val languageCode: String?
)
