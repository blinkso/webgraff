package ua.blink.whatsappgraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Chat(
    @JsonProperty("id")
    val id: Long,

    /**
     * PRIVATE, GROUP, SUPERGROUP, CHANNEL
     */
    @JsonProperty("type")
    val type: String,

    @JsonProperty("title")
    val title: String?,

    @JsonProperty("username")
    val username: String?,

    @JsonProperty("first_name")
    val firstName: String?,

    @JsonProperty("last_name")
    val lastName: String?,

    /**
     * Used for manual insert in handler state from User entity
     */
    @JsonProperty("language_code")
    var languageCode: String?
)