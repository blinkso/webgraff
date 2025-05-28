package ua.blink.webgraff.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.validation.Validator
import ua.blink.webgraff.autoconfigure.property.Properties
import ua.blink.webgraff.autoconfigure.property.PropertiesValidator
import ua.blink.webgraff.client.PollingClient
import ua.blink.webgraff.client.WebhookClient
import ua.blink.webgraff.component.ConversationApi

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