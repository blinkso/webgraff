package ua.blink.webgraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
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

    fun formAttributes(): String {
        val actions = (buttons as? MarkupInlinedReplyKeyboard)?.buttons.orEmpty()
            .filterIsInstance<InlineUrlReplyKeyboard>()
            .mapIndexed { index, button ->
                mapOf("id" to index.toString(), "label" to button.text,
                    "callback" to button.callbackData, "url" to button.url)
            }
        val mapper = ObjectMapper()
        val attributes = mapOf("version" to 1, "actions" to actions) + metadata
        val serialized = mapper.writeValueAsString(mapOf("webchat" to attributes))
        // The gateway retains complete labels/actions; Twilio attributes have a smaller limit.
        return if (serialized.toByteArray(Charsets.UTF_8).size <= 3500) serialized else
            mapper.writeValueAsString(mapOf("webchat" to (attributes - "actions" + ("actionsInJourney" to true))))
    }

    fun formBody(): String = text

    fun formContent(contentTemplates: Map<String, String>): Pair<String, String>? = null

    fun formShortenUrls(): Boolean = false
}
