package ua.blink.whatsappgraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Photo(
    @JsonProperty("file_id")
    val id: String,

    @JsonProperty("file_unique_id")
    val uniqueId: String?,

    @JsonProperty("width")
    val width: Int?,

    @JsonProperty("height")
    val height: Int?,

    @JsonProperty("file_size")
    val size: Long?
)