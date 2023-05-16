package ua.blink.telegraff.autoconfigure

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.validation.Validator
import ua.blink.telegraff.autoconfigure.property.TelegramProperties
import ua.blink.telegraff.autoconfigure.property.TelegramPropertiesValidator
import ua.blink.telegraff.client.TelegramPollingClient
import ua.blink.telegraff.client.TelegramWebhookClient
import ua.blink.telegraff.component.TelegramApi

/**
 * Enable Auto-Configuration for Telegraff.
 *
 */
@Configuration
@ConditionalOnClass(TelegramPollingClient::class, TelegramWebhookClient::class, TelegramApi::class)
@EnableConfigurationProperties(TelegramProperties::class)
@Import(TelegraffNonWebConfiguration::class, TelegraffServletWebConfiguration::class)
class TelegraffAutoConfiguration {

    companion object {
        @Bean
        fun configurationPropertiesValidator(): Validator {
            return TelegramPropertiesValidator()
        }
    }

}