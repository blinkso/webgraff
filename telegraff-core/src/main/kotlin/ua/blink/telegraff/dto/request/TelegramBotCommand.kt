package ua.blink.telegraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

class TelegramBotCommand(
    @get:JsonProperty("command")
    val command: String,

    @get:JsonProperty("description")
    val description: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TelegramBotCommand) return false

        if (command != other.command) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    override fun toString(): String {
        return "TelegramBotCommand(command='$command', description='$description')"
    }
}