package ua.blink.whatsappgraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Update(
    @JsonProperty("update_id")
    val id: Long,

    @JsonProperty("message")
    val message: Message?,

    @JsonProperty("edited_message")
    val editedMessage: Message?,

    @JsonProperty("channel_post")
    val channelPost: Message?,

    @JsonProperty("edited_channel_post")
    val editedChannelPost: Message?
)