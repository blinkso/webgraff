package me.ruslanys.telegraff.core.component

import me.ruslanys.telegraff.core.dto.*
import me.ruslanys.telegraff.core.dto.request.*
import me.ruslanys.telegraff.core.util.EMPTY
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap

class DefaultTelegramApi(telegramAccessKey: String, restTemplateBuilder: RestTemplateBuilder) : TelegramApi {

    private val restTemplate = restTemplateBuilder
        .rootUri("https://api.telegram.org/bot$telegramAccessKey")
        .build()
    private val fileRestTemplate = restTemplateBuilder
        .rootUri("https://api.telegram.org/file/bot$telegramAccessKey")
        .build()

    override fun getMe(): TelegramUser {
        val response = restTemplate.exchange(
            "/getMe",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<TelegramResponse<TelegramUser>>() {}
        )

        response.body?.description.takeIf { it?.isNotEmpty() == true }?.let { message ->
            log.error("getMe: $message")
        }

        return response.body!!.result!!
    }

    override fun getUpdates(offset: Long?, timeout: Int?): List<TelegramUpdate> {
        val params = hashMapOf<String, Any>()
        offset?.let { params["offset"] = it }
        timeout?.let { params["timeout"] = it }

        val response = restTemplate.exchange(
            "/getUpdates",
            HttpMethod.POST,
            HttpEntity(params),
            object : ParameterizedTypeReference<TelegramResponse<List<TelegramUpdate>>>() {}
        )

        response.body?.description.takeIf { it?.isNotEmpty() == true }?.let { message ->
            log.error("getUpdates: $message")
        }

        return response.body!!.result!!
    }

    override fun getFile(fileId: String): TelegramFile {
        val params = hashMapOf("file_id" to fileId)

        val response = restTemplate.exchange(
            "/getFile",
            HttpMethod.POST,
            HttpEntity(params),
            object : ParameterizedTypeReference<TelegramResponse<TelegramFile>>() {}
        )

        response.body?.description.takeIf { it?.isNotEmpty() == true }?.let { message ->
            log.error("getFile: $message")
        }

        return response.body!!.result!!
    }

    override fun getFileByPath(filePath: String): ByteArray {
        val params = mapOf<String, String>()

        val response = fileRestTemplate.exchange(
            "/$filePath",
            HttpMethod.GET,
            HttpEntity(params),
            object : ParameterizedTypeReference<ByteArray>() {}
        )

        return response.body!!
    }

    override fun setWebhook(url: String): Boolean {
        val params = hashMapOf("url" to url)

        val response = restTemplate.exchange(
            "/setWebhook",
            HttpMethod.POST,
            HttpEntity(params),
            object : ParameterizedTypeReference<TelegramResponse<Boolean>>() {}
        )

        response.body?.description.takeIf { it?.isNotEmpty() == true }?.let { message ->
            log.error("setWebhook: $message")
        }

        return response.body!!.result!!
    }

    override fun removeWebhook(): Boolean {
        return setWebhook(String.EMPTY)
    }

    override fun sendMessage(request: TelegramMessageSendRequest): TelegramMessage {
        val response = restTemplate.exchange(
            "/sendMessage",
            HttpMethod.POST,
            HttpEntity(request),
            object : ParameterizedTypeReference<TelegramResponse<TelegramMessage>>() {}
        )

        response.body?.description.takeIf { it?.isNotEmpty() == true }?.let { message ->
            log.error("sendMessage: $message")
        }

        return response.body!!.result!!
    }

    override fun sendDocument(request: TelegramDocumentSendRequest): TelegramMessage {
        val formData = createFormData(request).apply {
            add("document", object : ByteArrayResource(request.document) {
                override fun getFilename(): String {
                    return request.name
                }
            })
        }

        // --
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val entity = restTemplate.exchange(
            "/sendDocument",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            object : ParameterizedTypeReference<TelegramResponse<TelegramMessage>>() {}
        )

        return entity.body!!.result!!
    }

    override fun sendPhoto(request: TelegramPhotoSendRequest): TelegramMessage {
        val formData = createFormData(request).apply {
            add("photo", object : ByteArrayResource(request.photo) {
                override fun getFilename(): String {
                    return "picture.png"
                }
            })
        }

        // --
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val entity = restTemplate.exchange(
            "/sendPhoto",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            object : ParameterizedTypeReference<TelegramResponse<TelegramMessage>>() {}
        )

        return entity.body!!.result!!
    }

    override fun sendVoice(request: TelegramVoiceSendRequest): TelegramMessage {
        val formData = createFormData(request).apply {
            add("voice", object : ByteArrayResource(request.voice) {
                override fun getFilename(): String {
                    return "voice.mp3"
                }
            })
        }

        // --
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val entity = restTemplate.exchange(
            "/sendVoice",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            object : ParameterizedTypeReference<TelegramResponse<TelegramMessage>>() {}
        )

        return entity.body!!.result!!
    }

    override fun sendChatAction(request: TelegramChatActionRequest): Boolean {
        val response = restTemplate.exchange(
            "/sendChatAction",
            HttpMethod.POST,
            HttpEntity(request),
            object : ParameterizedTypeReference<TelegramResponse<Boolean>>() {}
        )

        response.body?.description.takeIf { it?.isNotEmpty() == true }?.let { message ->
            log.error("sendChatAction: $message")
        }

        return response.body!!.result!!
    }

    override fun sendAnswerCallbackQuery(callbackQueryId: Long): Boolean {
        val response = restTemplate.exchange(
            "/answerCallbackQuery",
            HttpMethod.POST,
            HttpEntity(
                LinkedMultiValueMap<String, Any>().apply {
                    add("callback_query_id", callbackQueryId)
                }
            ),
            object : ParameterizedTypeReference<TelegramResponse<Boolean>>() {}
        )

        response.body?.description.takeIf { it?.isNotEmpty() == true }?.let { message ->
            log.error("sendAnswerCallbackQuery: $message")
        }

        return response.body!!.result!!
    }

    private fun createFormData(request: TelegramMediaSendRequest): LinkedMultiValueMap<String, Any> =
        LinkedMultiValueMap<String, Any>().apply {
            add("chat_id", request.chatId)
            add("reply_markup", request.replyKeyboard)
            add("disable_notification", request.disableNotification)
            request.caption?.let { add("caption", request.caption) }
            request.parseMode?.let { add("parse_mode", request.parseMode.name) }
        }

    private companion object {
        private val log = LoggerFactory.getLogger(DefaultTelegramApi::class.java)
    }
}