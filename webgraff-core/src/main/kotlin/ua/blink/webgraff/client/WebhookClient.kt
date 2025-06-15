package ua.blink.webgraff.client

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ua.blink.webgraff.component.ConversationApi
import ua.blink.webgraff.dto.Message
import ua.blink.webgraff.dto.Update
import ua.blink.webgraff.event.UpdateEvent

@RestController
class WebhookClient(
    private val webhookUrl: String,
    private val conversationApi: ConversationApi,
    private val publisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper
) : Client {

    @PostConstruct
    override fun start() {
        log.info("Client: WEBHOOK")
        log.info("Setting webhook at: {}", webhookUrl)
        conversationApi.setWebhook(webhookUrl)
    }

    @PreDestroy
    override fun shutdown() {
        log.info("Removing webhook")
        conversationApi.removeWebhook()
    }

    override fun onUpdate(update: Message) {
        log.info("Got a new event: {}", update)
        publisher.publishEvent(UpdateEvent(this, update))
    }

    @RequestMapping(
        value = ["#{webChatProperties.getWebhookEndpointUrl()}"], 
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    fun update(@RequestParam params: Map<String, String>): ResponseEntity<String> {
        log.debug("Received webhook with parameters: {}", params)
        
        // Convert form parameters to Update object
        // Note: Twilio Conversations sends MessagingServiceSid, not ChatServiceSid
        val update = Update(
            messagingServiceSid = params["MessagingServiceSid"],
            eventType = params["EventType"],
            attributes = params["Attributes"],
            media = params["Media"],
            dateCreated = params["DateCreated"],
            index = params["Index"]?.toIntOrNull(),
            chatServiceSid = null, // Not sent by Twilio Conversations webhooks
            messageSid = params["MessageSid"],
            accountSid = params["AccountSid"],
            source = params["Source"],
            retryCount = null, // Not sent by Twilio Conversations webhooks
            author = params["Author"],
            participantSid = params["ParticipantSid"],
            body = params["Body"],
            conversationSid = params["ConversationSid"]
        )
        
        log.debug("Created update object: {}", update)
        onUpdate(update.getMessage(objectMapper))
        return ResponseEntity.ok("ok")
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebhookClient::class.java)
    }

}