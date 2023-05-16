package ua.blink.telegraff.filter

import org.slf4j.LoggerFactory
import ua.blink.telegraff.component.TelegramApi
import ua.blink.telegraff.dsl.Handler
import ua.blink.telegraff.dsl.HandlerState
import ua.blink.telegraff.dsl.HandlersFactory
import ua.blink.telegraff.dto.TelegramChat
import ua.blink.telegraff.dto.TelegramMessage
import ua.blink.telegraff.dto.request.*
import ua.blink.telegraff.exception.CancelException
import ua.blink.telegraff.exception.FinishException
import ua.blink.telegraff.exception.ValidationException
import ua.blink.telegraff.filter.FilterOrders.Companion.HANDLERS_FILTER_ORDER
import ua.blink.telegraff.util.DEFAULT_LOCALE
import ua.blink.telegraff.util.localized
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@ua.blink.telegraff.annotation.TelegramFilterOrder(HANDLERS_FILTER_ORDER)
class HandlersFilter(
    private val telegramApi: TelegramApi,
    handlersFactory: HandlersFactory
) : TelegramFilter {

    private val handlers: Map<String, Handler> = handlersFactory.getHandlers()
    private val states: MutableMap<Long, HandlerState> = ConcurrentHashMap()

    override suspend fun handleMessage(
        message: TelegramMessage,
        chain: TelegramFilterChain
    ) {
        val handler = findHandler(message)
        if (handler == null) {
            chain.doFilter(message)
            return
        }

        val state = states[message.chat.id]

        val response = try {
            if (state == null) {
                val newState = HandlerState(
                    chat = message.chat.apply {
                        // Populating language code in chat entity for further usage
                        languageCode = message.user?.languageCode
                    },
                    handler = handler
                )
                states[message.chat.id] = newState

                handleQuestion(newState)
            } else {
                handleContinuation(state, message)
            }
        } catch (e: CancelException) {
            clearState(message.chat)
            e.messageRequest
        } catch (e: Exception) {
            log.error("Error during handler processing", e)

            clearState(message.chat)
            val locale = Locale(message.user?.languageCode ?: DEFAULT_LOCALE.toLanguageTag())
            MarkdownMessage(
                "telegram_something_went_wrong".localized(locale),
                cancelButtonText = "telegram_cancel".localized(locale)
            )
        }

        sendResponse(message.chat, response)
    }

    fun clearState(chat: TelegramChat) {
        states.remove(chat.id)
    }

    private suspend fun handleContinuation(state: HandlerState, message: TelegramMessage): TelegramSendRequest? {
        val currentStep = state.currentStep!!
        val text = message.getMessageText()!!

        // validation
        val validation = currentStep.validation

        val answer = try {
            validation(state, text, message.contact, message.photo)
        } catch (e: ValidationException) {
            val question = currentStep.question(state)
            return TelegramMessageSendRequest(0, e.message, TelegramParseMode.MARKDOWN, question.replyKeyboard)
        } catch (e: CancelException) {
            clearState(message.chat)
            return e.messageRequest
        }
        state.answers[currentStep.key] = answer

        try {
            // next step
            val nextStepKey = currentStep.next(state)
            val nextStep = nextStepKey?.let { state.handler.getStepByKey(nextStepKey) }
            state.currentStep = nextStep
        } catch (e: FinishException) {
            state.currentStep = null
        }

        return handleQuestion(state)
    }

    private suspend fun handleQuestion(state: HandlerState): TelegramSendRequest? {
        val currentStep = state.currentStep

        return if (currentStep != null) {
            currentStep.question(state)
        } else {
            handleFinalization(state)
        }
    }

    private suspend fun handleFinalization(state: HandlerState): TelegramSendRequest? {
        clearState(state.chat)
        return state.handler.process(state, state.answers)
    }

    private fun sendResponse(chat: TelegramChat, response: TelegramSendRequest?) {
        if (response != null && response.chatId == 0L) {
            response.chatId = chat.id
        }

        when (response) {
            is TelegramPaymentRequest ->
                telegramApi.sendPayment(response)

            is TelegramMessageSendRequest ->
                telegramApi.sendMessage(response)

            is TelegramVoiceSendRequest ->
                telegramApi.sendVoice(response)

            is TelegramPhotoSendRequest ->
                telegramApi.sendPhoto(response)

            is TelegramDocumentSendRequest ->
                telegramApi.sendDocument(response)
        }
    }

    private fun findHandler(message: TelegramMessage): Handler? {
        val text =
            message.getMessageText()
                ?.lowercase()
                ?: return null
        for (entry in handlers) {
            if (text.startsWith(entry.key)) {
                clearState(message.chat)
                return entry.value
            }
        }

        val state = states[message.chat.id]
        if (state != null) {
            return state.handler
        }

        return null
    }

    companion object {
        private val log = LoggerFactory.getLogger(HandlersFilter::class.java)
    }

}