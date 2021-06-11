package me.ruslanys.telegraff.core.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty
import me.ruslanys.telegraff.core.util.DEFAULT_LOCALE
import java.util.*

class TelegramMarkupContactReplyKeyboard(
    text: String,
    columns: Int = DEFAULT_COLUMNS_NUMBER,
    locale: Locale? = DEFAULT_LOCALE,

    @get:JsonProperty("resize_keyboard")
    val resizeKeyboard: Boolean = true,

    @get:JsonProperty("one_time_keyboard")
    val oneTimeKeyboard: Boolean = true
) : TelegramReplyKeyboard() {

    val keyboard: List<List<TelegramReplyKeyboard>> =
        listOf(
            TelegramContactReplyKeyboard(text),
            TelegramCancelReplyKeyboard(locale)
        ).asSequence()
            .chunked(columns)
            .toList()

    companion object {
        private const val DEFAULT_COLUMNS_NUMBER = 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TelegramMarkupContactReplyKeyboard

        if (resizeKeyboard != other.resizeKeyboard) return false
        if (oneTimeKeyboard != other.oneTimeKeyboard) return false
        if (keyboard != other.keyboard) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + resizeKeyboard.hashCode()
        result = 31 * result + oneTimeKeyboard.hashCode()
        result = 31 * result + keyboard.hashCode()
        return result
    }

}