package ua.blink.telegraff.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

class TelegramMarkupInlinedReplyKeyboard(
    inlines: List<TelegramInlineUrlReplyKeyboard>,
    columns: Int = DEFAULT_COLUMNS_NUMBER
) : TelegramReplyKeyboard() {

    @get:JsonProperty("inline_keyboard")
    val inlineKeyboard: List<List<TelegramReplyKeyboard>> =
        inlines.asSequence()
            .chunked(columns)
            .toList()

    companion object {
        private const val DEFAULT_COLUMNS_NUMBER = 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as TelegramMarkupInlinedReplyKeyboard

        if (inlineKeyboard != other.inlineKeyboard) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + inlineKeyboard.hashCode()
        return result
    }


}