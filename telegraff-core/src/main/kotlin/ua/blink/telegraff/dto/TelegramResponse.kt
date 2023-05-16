package ua.blink.telegraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TelegramResponse<T>(
    @JsonProperty("ok")
    val ok: Boolean,

    @JsonProperty("result")
    val result: T?,

    @JsonProperty("error_code")
    val errorCode: Int?,

    @JsonProperty("description")
    val description: String?
)
