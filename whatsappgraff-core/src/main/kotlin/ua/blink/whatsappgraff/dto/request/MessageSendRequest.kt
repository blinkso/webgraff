package ua.blink.whatsappgraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import ua.blink.whatsappgraff.dto.request.keyboard.ActionReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.ReplyKeyboard

open class MessageSendRequest(
    chatId: String,

    @get:JsonProperty("Body")
    val text: String,

    replyMarkup: ReplyKeyboard? = null,
) : SendRequest(chatId, replyMarkup) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageSendRequest) return false
        if (!super.equals(other)) return false

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }

    override fun toString(): String {
        return "MessageSendRequest(text='$text')"
    }

    fun formAttributes(): String? {
        return when (buttons) {
            is MarkupInlinedReplyKeyboard -> {
                val actionButtons =
                    buttons.buttons.filterIsInstance<ActionReplyKeyboard>().toSet()
                val urlButtons =
                    buttons.buttons.filterIsInstance<InlineUrlReplyKeyboard>().filter { it.url != null }.toSet()
                val buttons =
                    buttons.buttons
                        .minus(actionButtons)
                        .minus(urlButtons)
                when {
                    buttons.size <= 3 && this !is MarkdownMessage -> {
                        val attributes = buttons
                            .withIndex()
                            .joinToString(prefix = "{", postfix = "}") { (index, button) ->
                                button as InlineUrlReplyKeyboard
                                "\"${index + 2}\":\"${button.callbackData ?: ""}\""
                            }

                        attributes
                    }

                    else -> {
                        val attributes = buttons
                            .withIndex()
                            .joinToString(prefix = "{", postfix = "}") { (index, button) ->
                                button as InlineUrlReplyKeyboard
                                "\"${index + 3}\":\"${button.callbackData ?: ""}\""
                            }

                        attributes
                    }
                }
            }

            else -> {
                null
            }
        }
    }

    fun formBody(): String? {
        return when (buttons) {
            is MarkupInlinedReplyKeyboard -> {
                val actionButtons =
                    buttons.buttons.filterIsInstance<ActionReplyKeyboard>().toSet()
                val urlButtons =
                    buttons.buttons.filterIsInstance<InlineUrlReplyKeyboard>().filter { it.url != null }.toSet()
                val buttons =
                    buttons.buttons
                        .minus(actionButtons)
                        .minus(urlButtons)
                when {
                    urlButtons.isNotEmpty() -> {
                        val text = buildString {
                            append(text)
                            append("\n")
                            urlButtons.forEach { button ->
                                append("\n${button.text}: ${button.url}")
                            }
                        }

                        text
                    }

                    buttons.size <= 3 && this !is MarkdownMessage -> {
                        null
                    }

                    else -> {
                        null
                    }
                }
            }

            else -> {
                text
            }
        }
    }

    fun formContent(
        buttonTemplate: List<String>,
        listTemplate: List<String>
    ): Pair<String, String>? {
        return when (buttons) {
            is MarkupInlinedReplyKeyboard -> {
                val actionButtons =
                    buttons.buttons.filterIsInstance<ActionReplyKeyboard>().toSet()
                val urlButtons =
                    buttons.buttons.filterIsInstance<InlineUrlReplyKeyboard>().filter { it.url != null }.toSet()
                val buttons =
                    buttons.buttons
                        .minus(actionButtons)
                        .minus(urlButtons)
                when {
                    buttons.size <= 3 && this !is MarkdownMessage -> {
                        val variables = buildString {
                            append("{\"1\":\"${formBody() ?: text}\"")
                            buttons.forEachIndexed { index, button ->
                                button as InlineUrlReplyKeyboard
                                append(", \"${index + 2}\":\"${button.text.take(BUTTON_MAX_LENGTH)}\"")
                            }
                            append("}")
                        }

                        val contentSid = buttonTemplate[buttons.size.minus(1)]
                        contentSid to variables.replace("\\r?\\n|\\r".toRegex(), "  ")
                    }

                    else -> {
                        val variables = buildString {
                            append("{\"1\":\"${formBody() ?: text}\"")
                            append(", \"2\":\"${actionButtons.firstOrNull()?.text?.take(BUTTON_MAX_LENGTH) ?: ""}\"")
                            buttons.forEachIndexed { index, button ->
                                button as InlineUrlReplyKeyboard
                                append(", \"${index + 3}\":\"${button.text.take(LIST_ITEM_MAX_LENGTH)}\"")
                            }
                            append("}")
                        }

                        val contentSid = listTemplate[buttons.size.minus(1)]
                        contentSid to variables.replace("\\r?\\n|\\r".toRegex(), "  ")
                    }
                }
            }

            else -> {
                null
            }
        }
    }

    private companion object {
        private const val BUTTON_MAX_LENGTH = 20
        private const val LIST_ITEM_MAX_LENGTH = 24
    }
}