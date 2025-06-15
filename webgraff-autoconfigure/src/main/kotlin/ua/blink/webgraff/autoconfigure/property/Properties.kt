package ua.blink.webgraff.autoconfigure.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

@Component("webChatProperties")
@ConfigurationProperties(prefix = "webchat", ignoreUnknownFields = false)
class Properties {

    /**
     * Twilio Access Key.
     */
    var accessKey: String = ""

    /**
     * Twilio Account SID.
     */
    var accountSid: String = ""

    /**
     * Twilio Conversations Service SID.
     */
    var serviceSid: String = ""

    /**
     * Twilio Messaging Service SID (optional).
     */
    var messagingSid: String = ""

    /**
     * Content templates for different layouts
     */
    var contentTemplates: Map<String, String> = mapOf()

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
    
    fun getWebhookEndpointUrl(): String = webhookEndpointUrl

    /**
     * Path where handlers declaration stored.
     */
    var handlersPath = "handlers"

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
