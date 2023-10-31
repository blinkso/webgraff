package ua.blink.whatsappgraff.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

class MarkupInlinedReplyKeyboard(
    inlines: List<InlineUrlReplyKeyboard>,
    columns: Int = DEFAULT_COLUMNS_NUMBER
) : ReplyKeyboard() {

    @get:JsonProperty("inline_keyboard")
    val inlineKeyboard: List<List<ReplyKeyboard>> =
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

        other as MarkupInlinedReplyKeyboard

        if (inlineKeyboard != other.inlineKeyboard) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + inlineKeyboard.hashCode()
        return result
    }


}