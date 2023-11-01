package ua.blink.whatsappgraff.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

class MarkupReplyKeyboard(
    answers: List<String>,
    cancelButtonText: String? = null,
) : ReplyKeyboard() {

    @get:JsonProperty("buttons")
    val buttons: List<ReplyKeyboard> = answers
        .map { InlineUrlReplyKeyboard(text = it, callbackData = it) }
        .let { keyboardButtons ->
            if (cancelButtonText != null) {
                keyboardButtons.plusElement(
                    InlineUrlReplyKeyboard(
                        text = cancelButtonText,
                        callbackData = cancelButtonText
                    )
                )
            } else {
                keyboardButtons
            }
        }
        .toList()

    companion object {
        private const val DEFAULT_COLUMNS_NUMBER = 2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MarkupReplyKeyboard) return false
        if (!super.equals(other)) return false

        if (buttons != other.buttons) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + buttons.hashCode()
        return result
    }


}