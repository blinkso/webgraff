package me.ruslanys.telegraff.core.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty
import me.ruslanys.telegraff.core.util.DEFAULT_LOCALE
import me.ruslanys.telegraff.core.util.localized
import java.util.*

class TelegramCancelReplyKeyboard(
    locale: Locale? = DEFAULT_LOCALE,
    @get:JsonProperty("text")
    val text: String = "cancel_telegram".localized(locale)
) : TelegramReplyKeyboard() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TelegramCancelReplyKeyboard

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }
}