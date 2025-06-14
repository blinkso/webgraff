package ua.blink.webgraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import ua.blink.webgraff.dto.request.keyboard.ActionReplyKeyboard
import ua.blink.webgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.webgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard
import ua.blink.webgraff.dto.request.keyboard.ReplyKeyboard

open class MessageSendRequest(
    chatId: String,

    to: String,

    @get:JsonProperty("Body")
    val text: String,

    replyMarkup: ReplyKeyboard? = null,
) : SendRequest(chatId = chatId, to = to, buttons = replyMarkup) {

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
                val actionButton =
                    buttons.buttons.firstOrNull { it is ActionReplyKeyboard } as? ActionReplyKeyboard
                val buttons = buttons.buttons.let { buttons ->
                    actionButton?.let { buttons.minusElement(it) } ?: buttons
                }
                when {
                    this is MarkdownTemplateMessage -> {
                        val attributes = buttons
                            .withIndex()
                            .joinToString(prefix = "{", postfix = "}") { (index, button) ->
                                button as InlineUrlReplyKeyboard
                                "\"${index + 1}\":\"${button.callbackData ?: ""}\""
                            }

                        attributes
                    }

                    this is MarkdownInlinedButtonsTemplateMessage -> {
                        val attributes = buttons
                            .withIndex()
                            .joinToString(prefix = "{", postfix = "}") { (index, button) ->
                                button as InlineUrlReplyKeyboard
                                "\"${index + 1}\":\"${button.url ?: ""}\""
                            }

                        attributes
                    }

                    buttons.any { (it as? InlineUrlReplyKeyboard)?.url != null } -> {
                        null
                    }

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
                val actionButton =
                    buttons.buttons.firstOrNull { it is ActionReplyKeyboard } as? ActionReplyKeyboard
                val buttons = buttons.buttons.let { buttons ->
                    actionButton?.let { buttons.minusElement(it) } ?: buttons
                }
                when {
                    buttons.any { (it as? InlineUrlReplyKeyboard)?.url != null } && this !is MarkdownInlinedButtonsTemplateMessage -> {
                        val text = buildString {
                            append(text)
                            append("\n")
                            buttons.forEach { button ->
                                button as InlineUrlReplyKeyboard
                                append("\n${button.text}: ${button.url}")
                            }
                        }

                        text
                    }

                    buttons.size <= 3 && this !is MarkdownMessage -> {
                        val text = buildString {
                            append(text)
                            buttons.forEachIndexed { index, button ->
                                button as InlineUrlReplyKeyboard
                                append("\n${index + 1}. ${button.text}")
                            }
                        }
                        text
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

    fun formContent(contentTemplates: Map<String, String>): Pair<String, String>? {
        // Always return null to force regular message format instead of content templates
        // This ensures button text is always included in the message body
        return null
    }

    fun formShortenUrls(): Boolean? {
        return when (buttons) {
            is MarkupInlinedReplyKeyboard -> {
                val actionButton =
                    buttons.buttons.firstOrNull { it is ActionReplyKeyboard } as? ActionReplyKeyboard
                val buttons = buttons.buttons.let { buttons ->
                    actionButton?.let { buttons.minusElement(it) } ?: buttons
                }
                when {
                    buttons.any { (it as? InlineUrlReplyKeyboard)?.url != null } -> {
                        true
                    }

                    buttons.size <= 3 && this !is MarkdownMessage -> {
                        false
                    }

                    else -> {
                        false
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