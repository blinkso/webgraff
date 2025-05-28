package ua.blink.webgraff.client

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ua.blink.webgraff.component.ConversationApi
import ua.blink.webgraff.dto.Update
import ua.blink.webgraff.event.UpdateEvent

/**
 * Controller for handling web chat operations.
 * This includes webhook handling for incoming messages and conversation management.
 */
@RestController
class WebChatController(
    private val webhookUrl: String,
    private val flexFlowSid: String,
    private val conversationApi: ConversationApi,
    private val publisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper
) : Client {

    @PostConstruct
    override fun start() {
        log.info("Client: WEB CHAT")
        log.info("Setting webhook at: {}", webhookUrl)
        conversationApi.setWebhook(webhookUrl)
    }

    @PreDestroy
    override fun shutdown() {
        log.info("Removing webhook")
        conversationApi.removeWebhook()
    }

    override fun onUpdate(update: ua.blink.webgraff.dto.Message) {
        log.info("Got a new event: {}", update)
        publisher.publishEvent(UpdateEvent(this, update))
    }
    
    /**
     * Provides configuration for the Flex Web Chat widget.
     * This endpoint is called when initializing the chat widget on your site.
     * 
     * @param eventId Optional event ID to include in the chat context
     * @return Configuration details for the Flex widget
     */
    @GetMapping("/api/chat-config")
    fun getChatConfig(@RequestParam(required = false) eventId: String?): ResponseEntity<Map<String, Any>> {
        val config = mutableMapOf<String, Any>(
            "flexFlowSid" to flexFlowSid
        )
        
        eventId?.let {
            config["context"] = mapOf(
                "eventId" to it
            )
        }
        
        return ResponseEntity.ok(config)
    }
    
    /**
     * Creates a new conversation for a web chat client.
     * This endpoint is called when initializing a chat for a user.
     * 
     * @param identity The identity of the client
     * @param eventId The event ID related to this conversation
     * @param attributes Additional attributes for the conversation
     * @return The created conversation details
     */
    @PostMapping("/api/create-conversation")
    fun createConversation(
        @RequestParam identity: String,
        @RequestParam(required = false) eventId: String?,
        @RequestParam(required = false) attributes: String?
    ): ResponseEntity<Map<String, String>> {
        try {
            log.info("Creating web chat conversation for identity: {}, event: {}", identity, eventId)
            
            val attributesJson = if (eventId != null) {
                """{"eventId": "$eventId"${attributes?.let { ", $it" } ?: ""}}"""
            } else {
                attributes
            }
            
            val conversation = conversationApi.createConversation(
                identity = identity,
                attributes = attributesJson
            )
            
            return ResponseEntity.ok(mapOf(
                "conversationSid" to (conversation.chatId ?: ""),
                "status" to "created"
            ))
        } catch (e: Exception) {
            log.error("Error creating web chat conversation", e)
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to e.message.toString()))
        }
    }

    /**
     * Handles webhook events from Twilio Conversations.
     * Processes incoming messages and publishes them as events.
     * 
     * @param update The update event from Twilio
     * @return A simple acknowledgement response
     */
    @RequestMapping("\${webchat.webhook-endpoint-url}")
    fun handleWebhook(@RequestBody update: Update): ResponseEntity<String> {
        log.info("Received web chat webhook: {}", update)
        
        val message = update.getMessage(objectMapper)
        
        log.info("Publishing web chat event: {}", message)
        onUpdate(message)
        
        return ResponseEntity.ok("ok")
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebChatController::class.java)
    }
} 