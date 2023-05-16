package ua.blink.telegraff.filter

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ua.blink.telegraff.dto.TelegramChat
import ua.blink.telegraff.dto.TelegramMessage
import ua.blink.telegraff.event.TelegramUpdateEvent
import ua.blink.telegraff.util.TelegramFilterOrderUtil
import kotlin.coroutines.CoroutineContext

class DefaultTelegramFiltersFactory(filters: List<TelegramFilter>) :
    TelegramFiltersFactory,
    TelegramFilterProcessor,
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.IO

    private val filters: List<TelegramFilter> = filters.sortedBy {
        TelegramFilterOrderUtil.getOrder(it::class.java)
    }

    override fun getFilters(): List<TelegramFilter> {
        return filters
    }

    override fun process(event: TelegramUpdateEvent) {
        launch {
            val chain = DefaultTelegramFilterChain(filters)
            if (event.update.message != null) { // only new messages are supported
                chain.doFilter(event.update.message)
            } else if (event.update.callbackQuery?.message != null) {
                // Populating callback query for further filtering
                event.update.callbackQuery.message.callbackQuery = event.update.callbackQuery
                // Currently we use callback data just to send it as a message text back and than handle it
                // Same as reply markup keyboard query
                event.update.callbackQuery.message.text = event.update.callbackQuery.data
                chain.doFilter(event.update.callbackQuery.message)
            } else if (event.update.preCheckoutQuery != null) {
                val message = TelegramMessage(
                    id = event.update.id,
                    user = event.update.preCheckoutQuery.user,
                    date = System.currentTimeMillis(),
                    preCheckoutQuery = event.update.preCheckoutQuery,
                    chat = TelegramChat(
                        id = event.update.preCheckoutQuery.user.id,
                        type = "",
                        title = null,
                        username = event.update.preCheckoutQuery.user.username,
                        firstName = event.update.preCheckoutQuery.user.firstName,
                        languageCode = event.update.preCheckoutQuery.user.languageCode,
                        lastName = event.update.preCheckoutQuery.user.lastName
                    ),
                    contact = null,
                    photo = null,
                    successfulPayment = null,
                    text = event.update.preCheckoutQuery.invoicePayload,
                    callbackQuery = null
                )
                chain.doFilter(message)
            }
        }
    }

}