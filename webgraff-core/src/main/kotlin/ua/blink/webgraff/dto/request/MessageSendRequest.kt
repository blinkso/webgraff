package ua.blink.webgraff.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
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

    private companion object {
        const val ATTRIBUTES_BUDGET_BYTES = 3500
        const val TWILIO_ATTRIBUTES_LIMIT_BYTES = 4096
        val JOURNEY_REFERENCE_KEYS = setOf("outputId", "revision", "responseTo")
    }

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
        if (serialized.toByteArray(Charsets.UTF_8).size <= ATTRIBUTES_BUDGET_BYTES) return serialized

        // JourneyConversationApi adds these references and checkpoints the full prompt before sending.
        // Keep only references: removing actions alone still leaves large catalogues and checkout data.
        val savedInJourney = !((metadata["outputId"] as? String).isNullOrBlank()) &&
            !((metadata["revision"] as? String).isNullOrBlank())
        val result = if (savedInJourney) mapper.writeValueAsString(mapOf("webchat" to (
            mapOf("version" to 1, "actionsInJourney" to true) +
                metadata.filterKeys { it in JOURNEY_REFERENCE_KEYS }
            ))) else serialized
        // Legacy messages have no saved copy: retain their actions instead of silently discarding them.
        val limit = if (savedInJourney) ATTRIBUTES_BUDGET_BYTES else TWILIO_ATTRIBUTES_LIMIT_BYTES
        require(result.toByteArray(Charsets.UTF_8).size <= limit) {
            "Webchat message attributes exceed $limit UTF-8 bytes"
        }
        return result
    }

    fun formBody(): String = text

    fun formContent(contentTemplates: Map<String, String>): Pair<String, String>? = null

    fun formShortenUrls(): Boolean = false
}
