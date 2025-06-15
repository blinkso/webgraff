package ua.blink.webgraff.client

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
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

    @RequestMapping(value = ["#{webChatProperties.getWebhookEndpointUrl()}"])
    fun update(@RequestBody params: Map<String, Any>): ResponseEntity<String> {
        log.info("Received JSON webhook with parameters: ${params}")

        // Convert JSON parameters to Update object
        val update = Update(
            messagingServiceSid = params["MessagingServiceSid"]?.toString(),
            eventType = params["EventType"]?.toString(),
            attributes = params["Attributes"]?.toString(),
            media = params["Media"]?.toString(),
            dateCreated = params["DateCreated"]?.toString(),
            index = params["Index"]?.toString()?.toIntOrNull(),
            chatServiceSid = params["ChatServiceSid"]?.toString(),
            messageSid = params["MessageSid"]?.toString(),
            accountSid = params["AccountSid"]?.toString(),
            source = params["Source"]?.toString(),
            retryCount = params["RetryCount"]?.toString()?.toIntOrNull(),
            clientIdentity = params["ClientIdentity"]?.toString(),
            author = params["Author"]?.toString(),
            participantSid = params["ParticipantSid"]?.toString(),
            body = params["Body"]?.toString(),
            conversationSid = params["ConversationSid"]?.toString()
        )

        log.info("Created update object from JSON: {}", update)
        val message = update.getMessage(objectMapper)
        log.info("Converted to message object: {}", message)
        onUpdate(message)
        return ResponseEntity.ok("ok")
    }

    @RequestMapping(
        value = ["#{webChatProperties.getWebhookEndpointUrl()}"],
        method = [RequestMethod.POST],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateJson(@RequestBody params: Map<String, Any>): ResponseEntity<String> {
        log.info("Received JSON webhook with parameters: ${params}")

        // Convert JSON parameters to Update object
        val update = Update(
            messagingServiceSid = params["MessagingServiceSid"]?.toString(),
            eventType = params["EventType"]?.toString(),
            attributes = params["Attributes"]?.toString(),
            media = params["Media"]?.toString(),
            dateCreated = params["DateCreated"]?.toString(),
            index = params["Index"]?.toString()?.toIntOrNull(),
            chatServiceSid = params["ChatServiceSid"]?.toString(),
            messageSid = params["MessageSid"]?.toString(),
            accountSid = params["AccountSid"]?.toString(),
            source = params["Source"]?.toString(),
            retryCount = params["RetryCount"]?.toString()?.toIntOrNull(),
            clientIdentity = params["ClientIdentity"]?.toString(),
            author = params["Author"]?.toString(),
            participantSid = params["ParticipantSid"]?.toString(),
            body = params["Body"]?.toString(),
            conversationSid = params["ConversationSid"]?.toString()
        )

        log.info("Created update object from JSON: {}", update)
        val message = update.getMessage(objectMapper)
        log.info("Converted to message object: {}", message)
        onUpdate(message)
        return ResponseEntity.ok("ok")
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebhookClient::class.java)
    }

}