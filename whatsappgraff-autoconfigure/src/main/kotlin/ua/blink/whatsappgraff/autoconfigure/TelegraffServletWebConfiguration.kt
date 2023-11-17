package ua.blink.whatsappgraff.autoconfigure

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import ua.blink.whatsappgraff.autoconfigure.property.Properties
import ua.blink.whatsappgraff.client.Client
import ua.blink.whatsappgraff.client.PollingClient
import ua.blink.whatsappgraff.client.WebhookClient
import ua.blink.whatsappgraff.component.ConversationApi
import ua.blink.whatsappgraff.component.DefaultConversationApi
import ua.blink.whatsappgraff.dsl.ButtonsFactory
import ua.blink.whatsappgraff.dsl.DefaultButtonsFactory
import ua.blink.whatsappgraff.dsl.DefaultHandlersFactory
import ua.blink.whatsappgraff.dsl.HandlersFactory
import ua.blink.whatsappgraff.filter.*

/**
 * Configuration for Telegraff when used in a servlet web context.
 *
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(PollingClient::class, WebhookClient::class)
class TelegraffServletWebConfiguration(@Qualifier("whatsappProperties") val properties: Properties) {

    @Bean
    fun objectMapper(): ObjectMapper {
        return Jackson2ObjectMapperBuilder()
            .modulesToInstall(KotlinModule.Builder().build())
            .build<ObjectMapper>()
            .apply { configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) }
    }

    @Bean
    @ConditionalOnMissingBean(ConversationApi::class)
    fun api(): ConversationApi {
        return DefaultConversationApi(
            accessKey = properties.accessKey,
            accountSid = properties.accountSid,
            serviceSid = properties.serviceSid,
            buttonTemplate = properties.buttonTemplate,
            listTemplate = properties.listTemplate
        )
    }

    @Bean
    @ConditionalOnMissingBean(name = ["whatsappProperties"])
    fun properties(): Properties = properties

    // region Clients

    @Bean
    @ConditionalOnMissingBean(Client::class)
    @ConditionalOnProperty(name = ["whatsapp.mode"], havingValue = "polling", matchIfMissing = true)
    fun pollingClient(
        conversationApi: ConversationApi,
        publisher: ApplicationEventPublisher
    ): PollingClient {
        return PollingClient(conversationApi = conversationApi, publisher = publisher)
    }

    @Bean
    @ConditionalOnMissingBean(Client::class)
    @ConditionalOnProperty(name = ["whatsapp.mode"], havingValue = "webhook")
    fun webhookClient(
        objectMapper: ObjectMapper,
        conversationApi: ConversationApi,
        publisher: ApplicationEventPublisher
    ): WebhookClient {
        return WebhookClient(
            webhookUrl = properties.getWebhookUrl(),
            conversationApi = conversationApi,
            publisher = publisher,
            objectMapper = objectMapper
        )
    }

    // endregion

    @Bean
    @ConditionalOnMissingBean(HandlersFactory::class)
    fun handlersFactory(context: GenericApplicationContext): DefaultHandlersFactory {
        return DefaultHandlersFactory(handlersPath = properties.handlersPath, context = context)
    }

    // region Filters

    @Bean
    @ConditionalOnMissingBean(FiltersFactory::class, FilterProcessor::class)
    fun filtersFactory(filters: List<Filter>): DefaultFiltersFactory {
        return DefaultFiltersFactory(filters)
    }

    @Bean
    @ConditionalOnMissingBean(ButtonsFactory::class)
    fun buttonsFactory(
        conversationApi: ConversationApi,
    ): DefaultButtonsFactory {
        return DefaultButtonsFactory()
    }

    @Bean
    @ConditionalOnMissingBean(HandlersFilter::class)
    fun handlersFilter(
        conversationApi: ConversationApi,
        buttonsFactory: ButtonsFactory,
        handlersFactory: HandlersFactory
    ): HandlersFilter {
        return HandlersFilter(
            conversationApi = conversationApi,
            buttonsFactory = buttonsFactory,
            handlersFactory = handlersFactory
        )
    }
}