package ua.blink.webgraff.dto.request

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ua.blink.webgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.webgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard

class MessageSendRequestTest {
    private val mapper = ObjectMapper()
    private val references = mapOf("outputId" to "catalog-output", "revision" to "catalog-revision",
        "responseTo" to "change-tickets-0001")

    @Test fun `original Le Galop six-option catalogue uses a bounded journey reference`() {
        val metadata: Map<String, Any?> = javaClass.getResourceAsStream("/le-galop-six-options.json")!!.use {
            mapper.readValue(it, object : TypeReference<Map<String, Any?>>() {})
        }
        assertEquals(6, (metadata["ticketOptions"] as List<*>).size)
        assertTrue(mapper.writeValueAsBytes(mapOf("webchat" to metadata)).size > 4096)
        assertReference(request(metadata))
    }

    @Test fun `large available catalogue and checkout data do not leak into compact attributes`() {
        val request = request(references + mapOf(
            "ticketOptions" to (1..17).map { mapOf("title" to "Квиток $it", "description" to "ї🎟️".repeat(60), "maximum" to 100) },
            "checkout" to mapOf("items" to List(17) { mapOf("description" to "Участь у події".repeat(15)) }),
            "futurePresentation" to "x".repeat(10000)))
        assertReference(request)
    }

    @Test fun `small messages retain complete metadata and button callbacks`() {
        val request = MessageSendRequest("chat", "identity", "Choose",
            MarkupInlinedReplyKeyboard(listOf(InlineUrlReplyKeyboard("Full ticket name", callbackData = "/buy"),
                InlineUrlReplyKeyboard("Event page", url = "https://eventmate.app/"))))
        request.metadata = references + mapOf("kind" to "cart", "checkout" to mapOf("total" to 6000))
        val attributes = mapper.readTree(request.formAttributes()).path("webchat")
        assertEquals(1, attributes.path("version").intValue())
        assertEquals("Full ticket name", attributes.path("actions")[0].path("label").textValue())
        assertEquals("/buy", attributes.path("actions")[0].path("callback").textValue())
        assertEquals("https://eventmate.app/", attributes.path("actions")[1].path("url").textValue())
        assertEquals(6000, attributes.path("checkout").path("total").intValue())
        assertFalse(attributes.has("actionsInJourney"))
    }

    @Test fun `fallback counts UTF-8 bytes rather than characters`() {
        val request = request(references + mapOf("description" to "ї".repeat(1800)))
        val full = mapper.writeValueAsString(mapOf("webchat" to (mapOf("version" to 1, "actions" to emptyList<Any>()) + request.metadata)))
        assertTrue(full.length < 3500)
        assertTrue(full.toByteArray(Charsets.UTF_8).size > 3500)
        assertReference(request)
    }

    @Test fun `journey fallback begins exactly above the byte budget`() {
        val request = sizedRequest(3500, references)
        assertEquals(3500, request.formAttributes().toByteArray(Charsets.UTF_8).size)
        assertTrue(mapper.readTree(request.formAttributes()).path("webchat").has("padding"))
        request.metadata = request.metadata + ("padding" to (request.metadata["padding"].toString() + "x"))
        assertReference(request)
    }

    @Test fun `legacy metadata is preserved up to the actual transport limit`() {
        val request = sizedRequest(4096, emptyMap())
        assertEquals(4096, request.formAttributes().toByteArray(Charsets.UTF_8).size)
        assertTrue(mapper.readTree(request.formAttributes()).path("webchat").has("padding"))
        request.metadata = request.metadata + ("padding" to (request.metadata["padding"].toString() + "x"))
        val error = assertThrows<IllegalArgumentException> { request.formAttributes() }
        assertEquals("Webchat message attributes exceed 4096 UTF-8 bytes", error.message)
    }

    @Test fun `oversized legacy buttons are never silently removed`() {
        val request = MessageSendRequest("chat", "identity", "Choose",
            MarkupInlinedReplyKeyboard(listOf(InlineUrlReplyKeyboard("Long label".repeat(600), callbackData = "/buy"))))
        assertThrows<IllegalArgumentException> { request.formAttributes() }
    }

    @Test fun `a partial or empty journey reference cannot discard content`() {
        for (metadata in listOf(mapOf("outputId" to "out"), mapOf("revision" to "rev"),
            mapOf("outputId" to "out", "revision" to " "))) {
            assertThrows<IllegalArgumentException> { request(metadata + ("padding" to "x".repeat(5000))).formAttributes() }
        }
    }

    @Test fun `final guard also checks oversized reference fields`() {
        val request = request(references + ("responseTo" to "ї".repeat(3000)))
        val error = assertThrows<IllegalArgumentException> { request.formAttributes() }
        assertEquals("Webchat message attributes exceed 3500 UTF-8 bytes", error.message)
    }

    private fun request(metadata: Map<String, Any?>) = MessageSendRequest("chat", "identity", "Choose").apply {
        this.metadata = metadata
    }

    private fun sizedRequest(bytes: Int, metadata: Map<String, Any?>): MessageSendRequest {
        val request = request(metadata + ("padding" to ""))
        val overhead = request.formAttributes().toByteArray(Charsets.UTF_8).size
        request.metadata = request.metadata + ("padding" to "x".repeat(bytes - overhead))
        return request
    }

    private fun assertReference(request: MessageSendRequest) {
        val original = mapper.writeValueAsString(request.metadata)
        val serialized = request.formAttributes()
        assertTrue(serialized.toByteArray(Charsets.UTF_8).size <= 3500)
        assertEquals(mapper.valueToTree<com.fasterxml.jackson.databind.JsonNode>(
            mapOf("version" to 1, "actionsInJourney" to true) + references), mapper.readTree(serialized).path("webchat"))
        assertEquals(original, mapper.writeValueAsString(request.metadata), "Compaction must not mutate the saved presentation")
    }
}
