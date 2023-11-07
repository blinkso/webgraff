package ua.blink.whatsappgraff.component

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import ua.blink.whatsappgraff.dto.Conversation
import ua.blink.whatsappgraff.dto.Media
import ua.blink.whatsappgraff.dto.Message
import ua.blink.whatsappgraff.dto.Response
import ua.blink.whatsappgraff.dto.request.*
import ua.blink.whatsappgraff.dto.request.keyboard.ActionReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard
import ua.blink.whatsappgraff.util.ImageType
import java.net.URLDecoder
import java.time.Duration
import java.time.Instant
import java.util.*

class DefaultConversationApi(
    accessKey: String,
    accountSid: String,
    private val serviceSid: String,
    private val buttonTemplate: List<String>,
    private val listTemplate: List<String>
) : ConversationApi {

    private val restTemplate = WebClient.builder()
        .baseUrl("https://conversations.twilio.com/v1")
        .defaultHeader(
            HttpHeaders.AUTHORIZATION,
            "Basic " + Base64.getEncoder().encodeToString("$accountSid:$accessKey".toByteArray())
        )
        .build()
    private val fileRestTemplate = WebClient.builder()
        .baseUrl("https://mcs.us1.twilio.com/v1")
        .defaultHeader(
            HttpHeaders.AUTHORIZATION,
            "Basic " + Base64.getEncoder().encodeToString("$accountSid:$accessKey".toByteArray())
        )
//        .filter(ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
//            log.info("Request: ${clientRequest.method()} ${clientRequest.url()}")
//            clientRequest.headers()
//                .forEach { name, values -> values.forEach { value -> log.info("$name: $value") } }
//            Mono.just(clientRequest)
//        })
//        .filter(ExchangeFilterFunction.ofResponseProcessor { clientResponse ->
//            log.info("Response: ${clientResponse.statusCode()}")
//            clientResponse.headers().asHttpHeaders()
//                .forEach { name, values -> values.forEach { value -> log.info("$name: $value") } }
//            Mono.just(clientResponse)
//        })
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create()
                    .followRedirect(true) // Configure to follow redirects
            )
        )
        .build()

    override fun getUpdates(offset: String?, timeout: Long?): List<Message> {
        val allConversations = mutableListOf<Conversation>()
        val seenChatIds = mutableSetOf<String>() // To track unique chat IDs
        var nextConversationUrl: String? = "/Services/$serviceSid/Conversations?Page=0&PageSize=50"

        while (nextConversationUrl != null) {
            val conversationsResponse = restTemplate.get()
                .uri(nextConversationUrl)
                .retrieve()
                .onStatus(
                    { status -> status.isError },
                    { clientResponse -> clientResponse.handleError("getUpdates/Services/$serviceSid/Conversations($offset, $timeout)") }
                )
                .bodyToMono(Response::class.java)
                .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS)) ?: break

            val newConversations =
                conversationsResponse.conversations?.filter { it.chatId !in seenChatIds } ?: emptyList()

            allConversations.addAll(newConversations)
            seenChatIds.addAll(newConversations.map { it.chatId })

            nextConversationUrl = if (conversationsResponse.meta.nextPageUrl != null) {
                val decodedUrl = URLDecoder.decode(conversationsResponse.meta.nextPageUrl, "UTF-8")
                decodedUrl.split("https://conversations.twilio.com/v1").last()
            } else {
                null
            }
        }
        val allMessages = mutableListOf<Message>()

        for (conversation in allConversations) {
            val messagesResponse = restTemplate.get()
                .uri("/Services/$serviceSid/Conversations/${conversation.chatId}/Messages?Page=0&PageSize=5&Order=desc")
                .retrieve()
                .onStatus(
                    { status -> status.isError },
                    { clientResponse -> clientResponse.handleError("getUpdates/Services/$serviceSid/Conversations/${conversation.chatId}/Messages($offset, $timeout)") }
                )
                .bodyToMono(Response::class.java)
                .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS)) ?: break
            allMessages.addAll(messagesResponse.messages?.reversed() ?: listOf())
        }

        return allMessages.filter { it.user != "system" }
    }

    override fun getFileByPath(filePath: String): ByteArray {
        return fileRestTemplate
            .get()
            .uri("/Services/$serviceSid/Media/$filePath/Content")
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("getFileByPath($filePath)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<ByteArray>() {})
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
    }

    override fun setWebhook(url: String) {
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("PostWebhookUrl", url)
            add("Method", "POST")
            add("Filters", "onMessageAdded")
        }

        restTemplate
            .post()
            .uri("/Configuration/Webhooks")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Set content type to form urlencoded
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("setWebhook($url)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<String>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
    }

    override fun removeWebhook() {
        return setWebhook("")
    }

    override fun sendMessage(request: MessageSendRequest): Message {
        // Convert MessageSendRequest to form data
        val formData = formMessageData(request)

        return restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations/${request.chatId}/Messages")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Set content type to form urlencoded
            .body(BodyInserters.fromFormData(formData)) // Use form data
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendMessage($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Message>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
    }

    private fun formMessageData(request: MessageSendRequest): MultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            when (request.buttons) {
                is MarkupInlinedReplyKeyboard ->
                    when {
                        request.buttons.buttons.any { (it as? InlineUrlReplyKeyboard)?.url != null } -> {
                            val actionButton =
                                request.buttons.buttons.firstOrNull { it is ActionReplyKeyboard } as? ActionReplyKeyboard
                            val buttons = request.buttons.buttons.let { buttons ->
                                actionButton?.let { buttons.minusElement(it) } ?: buttons
                            }
                            val text = buildString {
                                append(request.text)
                                append("\n\n")
                                buttons.forEach { button ->
                                    button as InlineUrlReplyKeyboard
                                    append("${button.text}: ${button.url}")
                                }
                            }
                            add("Body", text)
                            log.info("request.buttons.buttons.any { (it as? InlineUrlReplyKeyboard)?.url != null }: \n\n$text")
                        }

                        request.buttons.buttons.size <= 3 && request !is MarkdownMessage -> {
                            val actionButton =
                                request.buttons.buttons.firstOrNull { it is ActionReplyKeyboard } as? ActionReplyKeyboard
                            val buttons = request.buttons.buttons.let { buttons ->
                                actionButton?.let { buttons.minusElement(it) } ?: buttons
                            }

                            val variables = buildString {
                                append("{\"1\":\"${request.text}\"")
                                buttons.forEachIndexed { index, button ->
                                    button as InlineUrlReplyKeyboard
                                    append(", \"${index + 2}\":\"${button.text.take(BUTTON_MAX_LENGTH)}\"")
                                }
                                append("}")
                            }

                            val attributes = buttons
                                .withIndex()
                                .joinToString(prefix = "{", postfix = "}") { (index, button) ->
                                    button as InlineUrlReplyKeyboard
                                    "\"${index + 2}\":\"${button.callbackData ?: ""}\""
                                }

                            val contentSid = buttonTemplate[buttons.size.minus(1)]

                            add("ContentSid", contentSid)
                            add("ContentVariables", variables.replace("\\r?\\n|\\r".toRegex(), "  "))
                            add("Attributes", attributes)
                            log.info("request.buttons.buttons.size <= 3 && request !is MarkdownMessage: \n\n$contentSid\n\n$variables\n\n$attributes")
                        }

                        else -> {
                            val actionButton =
                                request.buttons.buttons.firstOrNull { it is ActionReplyKeyboard } as? ActionReplyKeyboard
                            val buttons = request.buttons.buttons.let { buttons ->
                                actionButton?.let { buttons.minusElement(it) } ?: buttons
                            }

                            val variables = buildString {
                                append("{\"1\":\"${request.text}\"")
                                append(", \"2\":\"${actionButton?.text?.take(BUTTON_MAX_LENGTH) ?: ""}\"")
                                buttons.forEachIndexed { index, button ->
                                    button as InlineUrlReplyKeyboard
                                    append(", \"${index + 3}\":\"${button.text.take(LIST_ITEM_MAX_LENGTH)}\"")
                                }
                                append("}")
                            }

                            val attributes = buttons
                                .withIndex()
                                .joinToString(prefix = "{", postfix = "}") { (index, button) ->
                                    button as InlineUrlReplyKeyboard
                                    "\"${index + 3}\":\"${button.callbackData ?: ""}\""
                                }

                            val contentSid = listTemplate[buttons.size.minus(1)]

                            add("ContentSid", contentSid)
                            add("ContentVariables", variables.replace("\\r?\\n|\\r".toRegex(), "  "))
                            add("Attributes", attributes)
                            log.info("else: \n\n$contentSid\n\n$variables\n\n$attributes")
                        }
                    }

                else -> {
                    add("Body", request.text)
                    log.info("else: \n\n${request.text}")
                }
            }
        }
    }

    override fun sendDocument(request: DocumentSendRequest): Message {
        val mediaSid = uploadMedia(request.name, request.document, "application/octet-stream")

        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("Body", (request.caption ?: ""))
            add("MediaSid", mediaSid)
        }

        return restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations/${request.chatId}/Messages")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Set content type to form urlencoded
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendDocument($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Message>() {})
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
    }

    override fun sendPhoto(request: PhotoSendRequest): Message {
        val imageType =
            ImageType.fromByteArray(request.photo)
        val mediaSid =
            uploadMedia("picture${Instant.now().toEpochMilli()}.${imageType.extension}", request.photo, imageType.type)

        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("Body", (request.caption ?: ""))
            add("MediaSid", mediaSid)
        }

        return restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations/${request.chatId}/Messages")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Set content type to form urlencoded
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendPhoto($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Message>() {})
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
    }

    override fun sendVoice(request: VoiceSendRequest): Message {
        val mediaSid = uploadMedia("voice.mp3", request.voice, "audio/mpeg")

        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("Body", (request.caption ?: ""))
            add("MediaSid", mediaSid)
        }

        return restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations/${request.chatId}/Messages")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Set content type to form urlencoded
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendVoice($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Message>() {})
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
    }

    private fun uploadMedia(filename: String, fileBytes: ByteArray, mediaType: String): String {
        val formData = MultipartBodyBuilder().apply {
            part("Content", fileBytes)
                .filename(filename)
                .header(HttpHeaders.CONTENT_TYPE, mediaType)
        }.build()

        return fileRestTemplate
            .post()
            .uri("/Services/$serviceSid/Media")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(formData))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Media>() {})
            .toFuture()
            .get()
            .sid
    }

    private fun ClientResponse.handleError(logMarker: String): Mono<Throwable> {
        return bodyToMono(String::class.java).flatMap { errorBody ->
            val responseBody = "$logMarker ${statusCode()} $errorBody"
            val responseException = RuntimeException(responseBody)
            log.error(responseBody, responseException)
            Mono.error(responseException)
        }
    }

    private companion object {
        private val log = LoggerFactory.getLogger(DefaultConversationApi::class.java)
        private const val REQUEST_TIMEOUT_SECONDS = 10L
        private const val BUTTON_MAX_LENGTH = 20
        private const val LIST_ITEM_MAX_LENGTH = 24
        private const val MIN_BACKOFF_SECONDS = 2L
        private const val MAX_RETRY_ATTEMPTS = 3L
    }
}