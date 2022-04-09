package me.ruslanys.telegraff.core.filter

import me.ruslanys.telegraff.core.annotation.TelegramFilterOrder
import me.ruslanys.telegraff.core.component.TelegramApi
import me.ruslanys.telegraff.core.dsl.Handler
import me.ruslanys.telegraff.core.dsl.HandlerState
import me.ruslanys.telegraff.core.dsl.HandlersFactory
import me.ruslanys.telegraff.core.dto.TelegramChat
import me.ruslanys.telegraff.core.dto.TelegramMessage
import me.ruslanys.telegraff.core.dto.request.*
import me.ruslanys.telegraff.core.exception.CancelException
import me.ruslanys.telegraff.core.exception.ValidationException
import me.ruslanys.telegraff.core.filter.FilterOrders.Companion.HANDLERS_FILTER_ORDER
import me.ruslanys.telegraff.core.util.DEFAULT_LOCALE
import me.ruslanys.telegraff.core.util.localized
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@TelegramFilterOrder(HANDLERS_FILTER_ORDER)
class HandlersFilter(
    private val telegramApi: TelegramApi,
    handlersFactory: HandlersFactory
) : TelegramFilter {

    private val handlers: Map<String, Handler> = handlersFactory.getHandlers()
    private val states: MutableMap<Long, HandlerState> = ConcurrentHashMap()


    override fun handleMessage(message: TelegramMessage, chain: TelegramFilterChain) {
        val handler = findHandler(message)
        if (handler == null) {
            chain.doFilter(message)
            return
        }

        val state = states[message.chat.id]

        val response = try {
            if (state == null) {
                val newState = HandlerState(
                    message.chat.apply {
                        // Populating language code in chat entity for further usage
                        languageCode = message.user?.languageCode
                    },
                    handler
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

    private fun handleContinuation(state: HandlerState, message: TelegramMessage): TelegramSendRequest? {
        val currentStep = state.currentStep!!
        // In case if it was a contact request question phone number of user contact will be returned and validation block will receive telegram contact
        // for it's validation
        val text = if (message.contact != null) {
            message.contact.phoneNumber ?: message.text!!
        } else {
            message.text!!
        }

        // validation
        val validation = currentStep.validation

        val answer = try {
            validation(state, text, message.contact)
        } catch (e: ValidationException) {
            val question = currentStep.question(state)
            return TelegramMessageSendRequest(0, e.message, TelegramParseMode.MARKDOWN, question.replyKeyboard)
        } catch (e: CancelException) {
            clearState(message.chat)
            return e.messageRequest
        }
        state.answers[currentStep.key] = answer

        // next step
        val nextStepKey = currentStep.next(state)
        val nextStep = nextStepKey?.let { state.handler.getStepByKey(nextStepKey) }
        state.currentStep = nextStep

        return handleQuestion(state)
    }

    private fun handleQuestion(state: HandlerState): TelegramSendRequest? {
        val currentStep = state.currentStep

        return if (currentStep != null) {
            currentStep.question(state)
        } else {
            handleFinalization(state)
        }
    }

    private fun handleFinalization(state: HandlerState): TelegramSendRequest? {
        clearState(state.chat)
        return state.handler.process(state, state.answers)
    }

    private fun sendResponse(chat: TelegramChat, response: TelegramSendRequest?) {
        if (response != null && response.chatId == 0L) {
            response.chatId = chat.id
        }

        when (response) {
            is TelegramMessageSendRequest -> telegramApi.sendMessage(response)
            is TelegramVoiceSendRequest -> telegramApi.sendVoice(response)
            is TelegramPhotoSendRequest -> telegramApi.sendPhoto(response)
            is TelegramDocumentSendRequest -> telegramApi.sendDocument(response)
        }
    }

    private fun findHandler(message: TelegramMessage): Handler? {
        val text = message.text?.toLowerCase() ?: message.contact?.phoneNumber ?: return null
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