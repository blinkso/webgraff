package ua.blink.whatsappgraff.autoconfigure

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext
import ua.blink.whatsappgraff.autoconfigure.property.Properties
import ua.blink.whatsappgraff.client.Client
import ua.blink.whatsappgraff.client.PollingClient
import ua.blink.whatsappgraff.client.WebhookClient
import ua.blink.whatsappgraff.component.ConversationApi
import ua.blink.whatsappgraff.component.DefaultConversationApi
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
    @ConditionalOnMissingBean(ConversationApi::class)
    fun api(): ConversationApi {
        return DefaultConversationApi(
            accessKey = properties.accessKey,
            accountSid = properties.accountSid,
            serviceSid = properties.serviceSid
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
        return PollingClient(conversationApi, publisher)
    }

    @Bean
    @ConditionalOnMissingBean(Client::class)
    @ConditionalOnProperty(name = ["whatsapp.mode"], havingValue = "webhook")
    fun webhookClient(
        conversationApi: ConversationApi,
        publisher: ApplicationEventPublisher
    ): WebhookClient {
        // TODO: Reconfigure with one of the following approaches
        /*
        @Bean(name = ["/ruslanys"])
        fun ruslanController(): Controller {
            val aa = RequestMappingHandlerAdapter()
            val bb = RequestMappingHandlerMapping()
            return Controller { request, response ->
                response.writer.print("ok")

                ModelAndView("index")
                // null
            }
        }
        */
        return WebhookClient(conversationApi, publisher, properties.getWebhookUrl())
    }

    // endregion

    @Bean
    @ConditionalOnMissingBean(HandlersFactory::class)
    fun handlersFactory(context: GenericApplicationContext): DefaultHandlersFactory {
        return DefaultHandlersFactory(context, properties.handlersPath)
    }

    // region Filters

    @Bean
    @ConditionalOnMissingBean(FiltersFactory::class, FilterProcessor::class)
    fun filtersFactory(filters: List<Filter>): DefaultFiltersFactory {
        return DefaultFiltersFactory(filters)
    }

    @Bean
    @ConditionalOnMissingBean(HandlersFilter::class)
    fun handlersFilter(
        conversationApi: ConversationApi,
        handlersFactory: HandlersFactory
    ): HandlersFilter {
        return HandlersFilter(conversationApi, handlersFactory)
    }
}