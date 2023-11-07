package ua.blink.whatsappgraff.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.validation.Validator
import ua.blink.whatsappgraff.autoconfigure.property.Properties
import ua.blink.whatsappgraff.autoconfigure.property.PropertiesValidator
import ua.blink.whatsappgraff.client.PollingClient
import ua.blink.whatsappgraff.client.WebhookClient
import ua.blink.whatsappgraff.component.ConversationApi

/**
 * Enable Auto-Configuration for Telegraff.
 *
 */
@Configuration
@ConditionalOnClass(PollingClient::class, WebhookClient::class, ConversationApi::class)
@EnableConfigurationProperties(Properties::class)
@Import(TelegraffNonWebConfiguration::class, TelegraffServletWebConfiguration::class)
class TelegraffAutoConfiguration {

    companion object {
        @Bean
        fun configurationPropertiesValidator(): Validator {
            return PropertiesValidator()
        }
    }

}