package ua.blink.telegraff.component

import ua.blink.telegraff.dto.TelegramFile
import ua.blink.telegraff.dto.TelegramMessage
import ua.blink.telegraff.dto.TelegramUpdate
import ua.blink.telegraff.dto.TelegramUser
import ua.blink.telegraff.dto.request.*

interface TelegramApi {

    fun getMe(): TelegramUser

    fun getUpdates(offset: Long?, timeout: Int?): List<TelegramUpdate>

    fun getUpdates(offset: Long?): List<TelegramUpdate> {
        return getUpdates(offset, null)
    }

    fun getUpdates(): List<TelegramUpdate> {
        return getUpdates(null)
    }

    fun getFile(fileId: String): TelegramFile

    fun getFileByPath(filePath: String): ByteArray

    fun setWebhook(url: String): Boolean

    fun removeWebhook(): Boolean

    fun sendMessage(request: TelegramMessageSendRequest): TelegramMessage

    fun sendPayment(request: TelegramPaymentRequest): TelegramMessage

    fun sendPhoto(request: TelegramPhotoSendRequest): TelegramMessage

    fun sendDocument(request: TelegramDocumentSendRequest): TelegramMessage

    fun sendVoice(request: TelegramVoiceSendRequest): TelegramMessage

    fun sendChatAction(request: TelegramChatActionRequest): Boolean

    fun sendAnswerCallbackQuery(callbackQueryId: Long): Boolean

    fun sendAnswerPreCheckoutQuery(preCheckoutQueryId: Long, errorMessage: String? = null): Boolean

    fun setMyCommands(locale: String?, commands: List<TelegramBotCommand>): Boolean

    fun deleteMyCommands(locale: String?): Boolean
}
