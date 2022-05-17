package me.ruslanys.telegraff.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TelegramFile(
    @JsonProperty("file_id")
    val id: String,

    @JsonProperty("file_unique_id")
    val uniqueId: String?,

    @JsonProperty("file_path")
    val path: String?,

    @JsonProperty("file_size")
    val size: Long?
)