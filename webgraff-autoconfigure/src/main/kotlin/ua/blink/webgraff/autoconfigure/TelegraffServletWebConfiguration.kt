package ua.blink.webgraff.autoconfigure

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
import ua.blink.webgraff.autoconfigure.property.Properties
import ua.blink.webgraff.autoconfigure.property.WebChatMode
import ua.blink.webgraff.client.Client
import ua.blink.webgraff.client.PollingClient
import ua.blink.webgraff.client.WebChatController
import ua.blink.webgraff.component.ConversationApi
import ua.blink.webgraff.component.DefaultConversationApi
import ua.blink.webgraff.dsl.ButtonsFactory
import ua.blink.webgraff.dsl.DefaultButtonsFactory
import ua.blink.webgraff.dsl.DefaultHandlersFactory
import ua.blink.webgraff.dsl.HandlersFactory
import ua.blink.webgraff.filter.*

/**
 * Configuration for Telegraff when used in a servlet web context.
 *
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass(PollingClient::class, WebChatController::class)
class TelegraffServletWebConfiguration(@Qualifier("webChatProperties") val properties: Properties) {

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
            apiKeySid = properties.apiKeySid,
            apiKeySecret = properties.apiKeySecret,
            accountSid = properties.accountSid,
            serviceSid = properties.serviceSid,
            flexFlowSid = properties.flexFlowSid,
            buttonTemplate = properties.buttonTemplate,
            listTemplate = properties.listTemplate,
            tokenTtl = properties.tokenTtl
        )
    }

    @Bean
    @ConditionalOnMissingBean(name = ["webChatProperties"])
    fun properties(): Properties = properties

    // region Clients

    @Bean
    @ConditionalOnMissingBean(Client::class)
    @ConditionalOnProperty(name = ["webchat.mode"], havingValue = "polling", matchIfMissing = false)
    fun pollingClient(
        conversationApi: ConversationApi,
        publisher: ApplicationEventPublisher
    ): PollingClient {
        return PollingClient(conversationApi = conversationApi, publisher = publisher)
    }

    @Bean
    @ConditionalOnMissingBean(Client::class)
    @ConditionalOnProperty(name = ["webchat.mode"], havingValue = "webhook", matchIfMissing = true)
    fun webChatController(
        objectMapper: ObjectMapper,
        conversationApi: ConversationApi,
        publisher: ApplicationEventPublisher
    ): WebChatController {
        return WebChatController(
            webhookUrl = properties.getWebhookUrl(),
            flexFlowSid = properties.flexFlowSid,
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

    @Bean
    @ConditionalOnMissingBean(AttributesFilter::class)
    fun attributesFilter(
        buttonsFactory: ButtonsFactory
    ): AttributesFilter {
        return AttributesFilter(buttonsFactory = buttonsFactory)
    }
}