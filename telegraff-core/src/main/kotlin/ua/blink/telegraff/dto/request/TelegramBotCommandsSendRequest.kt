package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
class TelegramBotCommandsSendRequest(
    @get:JsonProperty("commands")
    val commands: List<TelegramBotCommand>? = null,

    @get:JsonProperty("language_code")
    val languageCode: String? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TelegramBotCommandsSendRequest) return false

        if (commands != other.commands) return false
        if (languageCode != other.languageCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = commands?.hashCode() ?: 0
        result = 31 * result + (languageCode?.hashCode() ?: 0)
        return result
    }
}