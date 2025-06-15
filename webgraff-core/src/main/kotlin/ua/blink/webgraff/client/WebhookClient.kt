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
    fun update(
        @RequestParam(value = "MessagingServiceSid", required = false) messagingServiceSid: String?,
        @RequestParam(value = "EventType", required = false) eventType: String?,
        @RequestParam(value = "Attributes", required = false) attributes: String?,
        @RequestParam(value = "Media", required = false) media: String?,
        @RequestParam(value = "DateCreated", required = false) dateCreated: String?,
        @RequestParam(value = "Index", required = false) index: String?,
        @RequestParam(value = "ChatServiceSid", required = false) chatServiceSid: String?,
        @RequestParam(value = "MessageSid", required = false) messageSid: String?,
        @RequestParam(value = "AccountSid", required = false) accountSid: String?,
        @RequestParam(value = "Source", required = false) source: String?,
        @RequestParam(value = "ClientIdentity", required = false) clientIdentity: String?,
        @RequestParam(value = "RetryCount", required = false) retryCount: String?,
        @RequestParam(value = "Author", required = false) author: String?,
        @RequestParam(value = "ParticipantSid", required = false) participantSid: String?,
        @RequestParam(value = "Body", required = false) body: String?,
        @RequestParam(value = "ConversationSid", required = false) conversationSid: String?
    ): ResponseEntity<String> {
        log.info("Received webhook with individual parameters: " +
                "messageSid=$messageSid, conversationSid=$conversationSid, body=$body, author=$author")

        // Convert parameters to Update object
        val update = Update(
            messagingServiceSid = messagingServiceSid,
            eventType = eventType,
            attributes = attributes,
            media = media,
            dateCreated = dateCreated,
            index = index?.toIntOrNull(),
            chatServiceSid = chatServiceSid,
            messageSid = messageSid,
            accountSid = accountSid,
            source = source,
            retryCount = retryCount?.toIntOrNull(),
            clientIdentity = clientIdentity,
            author = author,
            participantSid = participantSid,
            body = body,
            conversationSid = conversationSid
        )

        log.debug("Created update object: {}", update)
        onUpdate(update.getMessage(objectMapper))
        return ResponseEntity.ok("ok")
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebhookClient::class.java)
    }

}