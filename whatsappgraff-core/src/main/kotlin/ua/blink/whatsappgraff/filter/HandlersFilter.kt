package ua.blink.whatsappgraff.filter

import org.slf4j.LoggerFactory
import ua.blink.whatsappgraff.annotation.FilterOrder
import ua.blink.whatsappgraff.component.ConversationApi
import ua.blink.whatsappgraff.dsl.Handler
import ua.blink.whatsappgraff.dsl.HandlerState
import ua.blink.whatsappgraff.dsl.HandlersFactory
import ua.blink.whatsappgraff.dto.Message
import ua.blink.whatsappgraff.dto.request.*
import ua.blink.whatsappgraff.exception.CancelException
import ua.blink.whatsappgraff.exception.FinishException
import ua.blink.whatsappgraff.exception.ValidationException
import ua.blink.whatsappgraff.filter.FilterOrders.Companion.HANDLERS_FILTER_ORDER
import ua.blink.whatsappgraff.util.DEFAULT_LOCALE
import ua.blink.whatsappgraff.util.localized
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@FilterOrder(HANDLERS_FILTER_ORDER)
class HandlersFilter(
    private val conversationApi: ConversationApi,
    handlersFactory: HandlersFactory
) : Filter {

    private val handlers: Map<String, Handler> = handlersFactory.getHandlers()
    private val states: MutableMap<String, HandlerState> = ConcurrentHashMap()

    override suspend fun handleMessage(
        message: Message,
        chain: FilterChain
    ) {
        val handler = findHandler(message)
        if (handler == null) {
            chain.doFilter(message)
            return
        }

        val state = states[message.chatId]

        val response = try {
            if (state == null) {
                val newState = HandlerState(
                    chatId = message.chatId,
                    handler = handler
                )
                states[message.chatId] = newState

                handleQuestion(newState)
            } else {
                handleContinuation(state, message)
            }
        } catch (e: CancelException) {
            clearState(message.chatId)
            e.messageRequest
        } catch (e: Exception) {
            log.error("Error during handler processing", e)

            clearState(message.chatId)
            val locale = Locale(DEFAULT_LOCALE.toLanguageTag())
            MarkdownMessage(
                "telegram_something_went_wrong".localized(locale),
                cancelButtonText = "telegram_cancel".localized(locale)
            )
        }

        sendResponse(message.chatId, response)
    }

    fun clearState(chatId: String) {
        states.remove(chatId)
    }

    private suspend fun handleContinuation(state: HandlerState, message: Message): SendRequest? {
        val currentStep = state.currentStep!!
        val text = message.getMessageText()!!

        // validation
        val validation = currentStep.validation

        val answer = try {
            validation(state, text, message.photo)
        } catch (e: ValidationException) {
            val question = currentStep.question(state)
            return MessageSendRequest("", e.message, question.replyKeyboard)
        } catch (e: CancelException) {
            clearState(message.chatId)
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

    private suspend fun handleQuestion(state: HandlerState): SendRequest? {
        val currentStep = state.currentStep

        return if (currentStep != null) {
            currentStep.question(state)
        } else {
            handleFinalization(state)
        }
    }

    private suspend fun handleFinalization(state: HandlerState): SendRequest? {
        clearState(state.chatId)
        return state.handler.process(state, state.answers)
    }

    private fun sendResponse(chatId: String, response: SendRequest?) {
        if (response != null && response.chatId == "") {
            response.chatId = chatId
        }

        when (response) {
            is MessageSendRequest ->
                conversationApi.sendMessage(response)

            is VoiceSendRequest ->
                conversationApi.sendVoice(response)

            is PhotoSendRequest ->
                conversationApi.sendPhoto(response)

            is DocumentSendRequest ->
                conversationApi.sendDocument(response)
        }
    }

    private fun findHandler(message: Message): Handler? {
        val text =
            message.getMessageText()
                ?.lowercase()
                ?: return null
        for (entry in handlers) {
            if (text.startsWith(entry.key)) {
                clearState(message.chatId)
                return entry.value
            }
        }

        val state = states[message.chatId]
        if (state != null) {
            return state.handler
        }

        return null
    }

    companion object {
        private val log = LoggerFactory.getLogger(HandlersFilter::class.java)
    }

}