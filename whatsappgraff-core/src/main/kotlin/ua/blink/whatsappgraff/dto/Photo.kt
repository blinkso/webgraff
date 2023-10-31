package ua.blink.whatsappgraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Photo(
    @JsonProperty("sid")
    val id: String,

    @JsonProperty("filename")
    val uniqueId: String?,

    @JsonProperty("content_type")
    val contentType: String?,

    @JsonProperty("size")
    val size: Long?
)