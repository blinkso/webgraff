package ua.blink.webgraff.autoconfigure.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

@Component("webChatProperties")
@ConfigurationProperties(prefix = "webchat", ignoreUnknownFields = false)
class Properties {

    /**
     * Twilio API Key SID.
     */
    var apiKeySid: String = ""

    /**
     * Twilio API Key Secret.
     */
    var apiKeySecret: String = ""

    /**
     * Twilio Account SID.
     */
    var accountSid: String = ""

    /**
     * Twilio Conversations Service SID.
     */
    var serviceSid: String = ""

    /**
     * Twilio Flex Flow SID.
     */
    var flexFlowSid: String = ""

    /**
     * Button templates up to three buttons
     */
    var buttonTemplate: List<String> = listOf()

    /**
     * List templates up to three buttons
     */
    var listTemplate: List<String> = listOf()

    /**
     * Web chat updates mode.
     */
    var mode = WebChatMode.WEBHOOK

    /**
     * Webhook base URL.
     * For example, https://localhost:8443.
     */
    var webhookBaseUrl: String? = null

    /**
     * Webhook endpoint url.
     * For example, /webchat.
     */
    var webhookEndpointUrl: String = "/webchat/" + UUID.randomUUID().toString()

    /**
     * Path where handlers declaration stored.
     */
    var handlersPath = "handlers"

    /**
     * Token TTL in seconds.
     * Default is 3600 seconds (1 hour).
     */
    var tokenTtl: Int = 3600

    /**
     * UnresolvedMessageFilter properties.
     */
    var unresolvedFilter = UnresolvedMessageFilterProperties()


    fun getWebhookUrl(): String = "$webhookBaseUrl$webhookEndpointUrl"


    class UnresolvedMessageFilterProperties {
        /**
         * Enable UnresolvedMessageFilter.
         */
        var enabled: Boolean = true
    }

}
