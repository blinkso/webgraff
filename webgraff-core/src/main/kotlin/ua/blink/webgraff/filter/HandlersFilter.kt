package ua.blink.webgraff.filter

import org.slf4j.LoggerFactory
import ua.blink.webgraff.annotation.FilterOrder
import ua.blink.webgraff.component.ConversationApi
import ua.blink.webgraff.dsl.ButtonsFactory
import ua.blink.webgraff.dsl.Handler
import ua.blink.webgraff.dsl.HandlerState
import ua.blink.webgraff.dsl.HandlersFactory
import ua.blink.webgraff.dto.Message
import ua.blink.webgraff.dto.request.*
import ua.blink.webgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard
import ua.blink.webgraff.exception.CancelException
import ua.blink.webgraff.exception.FinishException
import ua.blink.webgraff.exception.ValidationException
import ua.blink.webgraff.filter.FilterOrders.Companion.HANDLERS_FILTER_ORDER
import ua.blink.webgraff.util.DEFAULT_LOCALE
import ua.blink.webgraff.util.localized
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@FilterOrder(HANDLERS_FILTER_ORDER)
class HandlersFilter(
    private val conversationApi: ConversationApi,
    private val buttonsFactory: ButtonsFactory,
    handlersFactory: HandlersFactory,
    private val stateChanged: (String, HandlerState?) -> Unit = { _, _ -> },
    private val validationFailed: (String) -> Unit = {}
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
                    chatId = message.chatId ?: "",
                    contact = message.user ?: "",
                    username = message.user ?: "",
                    handler = handler
                )
                states[message.chatId ?: ""] = newState
                checkpoint(newState)

                handleQuestion(newState)
            } else {
                handleContinuation(state, message)
            }
        } catch (e: CancelException) {
            clearState(message.chatId ?: "")
            e.messageRequest
        } catch (e: Exception) {
            // The transport owns retry/acknowledgement. Keep the current step on failure.
            throw e
        }

        sendResponse(chatId = message.chatId ?: "", to = message.user ?: "", response = response)
    }

    fun clearState(chatId: String) {
        states.remove(chatId)
        stateChanged(chatId, null)
    }

    fun currentState(chatId: String): HandlerState? = states[chatId]

    fun restoreState(chatId: String, command: String, step: String?, answers: Map<String, Any>,
                     attributes: Map<String, Any> = emptyMap(), username: String = ""): Boolean {
        val handler = handlers[command.lowercase()] ?: return false
        if (step != null && handler.getStepByKey(step) == null) return false
        states[chatId] = HandlerState(chatId, username, username, handler).apply {
            currentStep = step?.let(handler::getStepByKey)
            this.answers.putAll(answers)
            this.attributes.putAll(attributes)
        }
        return true
    }

    suspend fun resume(chatId: String): Boolean {
        val state = states[chatId] ?: return false
        // A final submission is only retried by the original input, never by opening a chat.
        val step = state.currentStep ?: return false
        sendResponse(chatId, state.username, step.question(state))
        return true
    }

    private fun checkpoint(state: HandlerState) = stateChanged(state.chatId, state)

    private suspend fun handleContinuation(state: HandlerState, message: Message): SendRequest? {
        val currentStep = state.currentStep ?: return handleFinalization(state)
        val text = message.getMessageText()!!

        // validation
        val validation = currentStep.validation

        val answer = try {
            validation(state, text, message.photo)
        } catch (e: ValidationException) {
            validationFailed(state.chatId)
            val question = currentStep.question(state)
            return MessageSendRequest(
                chatId = "",
                to = state.username,
                text = e.message,
                replyMarkup = question.buttons
            ).also { it.metadata = question.metadata }
        } catch (e: CancelException) {
            clearState(message.chatId ?: "")
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
        checkpoint(state)

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
        val response = state.handler.process(state, state.answers)
        clearState(state.chatId)
        return response
    }

    fun sendResponse(chatId: String, to: String, response: SendRequest?) {
        if (response != null && response.chatId == "") {
            response.chatId = chatId
            response.to = to
        }

        when (response) {
            is MessageSendRequest ->
                conversationApi.sendMessage(response).also {
                    if (response.buttons is MarkupInlinedReplyKeyboard &&
                        response !is MarkdownMessage &&
                        response !is MarkdownTemplateMessage
                    ) {
                        buttonsFactory.addButtonsRequest(response)
                    }
                }

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
            if (text == entry.key || text.startsWith(entry.key + "=") ||
                (message.attributes?.contains("\"action\"") == true && text.startsWith(entry.key))) {
                states.remove(message.chatId ?: "")
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
