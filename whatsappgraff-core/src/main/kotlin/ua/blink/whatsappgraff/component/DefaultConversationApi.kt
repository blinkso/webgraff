package ua.blink.whatsappgraff.component

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import ua.blink.whatsappgraff.dto.Conversation
import ua.blink.whatsappgraff.dto.Media
import ua.blink.whatsappgraff.dto.Message
import ua.blink.whatsappgraff.dto.Response
import ua.blink.whatsappgraff.dto.request.DocumentSendRequest
import ua.blink.whatsappgraff.dto.request.MessageSendRequest
import ua.blink.whatsappgraff.dto.request.PhotoSendRequest
import ua.blink.whatsappgraff.dto.request.VoiceSendRequest
import ua.blink.whatsappgraff.dto.request.keyboard.InlineUrlReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.MarkupInlinedReplyKeyboard
import ua.blink.whatsappgraff.dto.request.keyboard.MarkupReplyKeyboard
import java.net.URLDecoder
import java.time.Duration
import java.util.*

class DefaultConversationApi(
    accessKey: String,
    accountSid: String,
    private val serviceSid: String
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
        return fileRestTemplate.get()
            .uri("/Services/$serviceSid/Media/$filePath")
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
        val params = hashMapOf(
            "Configuration.Url" to url,
            "Configuration.Method" to "POST",
            "Configuration.Filters" to listOf("onMessageAdded"),
            "Target" to "webhook"
        )

        restTemplate
            .post()
            .uri("/Services/$serviceSid/Webhooks")
            .body(BodyInserters.fromValue(params))
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
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            when {
                request.buttons is MarkupInlinedReplyKeyboard && request.buttons.buttons.size <= 3 && request.buttons.buttons.all { it is InlineUrlReplyKeyboard && it.url == null } -> {
                    val contentSid = when (request.buttons.buttons.size) {
                        1 -> "HXf113a83d6951cca5f1583a4fb0d7a988"
                        2 -> "HX0d3631717c0597db8e63307ac792726e"
                        else -> "HX32ea167350eefa2e5a41a493a20a89bc"
                    }

                    val variables = buildString {
                        append("{\"1\":\"${request.text}\"")
                        request.buttons.buttons.forEachIndexed { index, button ->
                            button as InlineUrlReplyKeyboard
                            append(", \"${index + 2}\":\"${button.text.take(BUTTON_MAX_LENGTH)}\"")
                        }
                        append("}")
                    }

                    val attributes = request.buttons.buttons
                        .withIndex()
                        .joinToString(prefix = "{", postfix = "}") { (index, button) ->
                            button as InlineUrlReplyKeyboard
                            "\"${index + 2}\":\"${button.callbackData ?: ""}\""
                        }

                    add("ContentSid", contentSid)
                    add("ContentVariables", variables.replace("\\r?\\n|\\r".toRegex(), "  "))
                    add("Attributes", attributes)
                    log.info("TEST 1\n\n$contentSid\n\n$variables\n\n$attributes")
                }

                request.buttons is MarkupInlinedReplyKeyboard && request.buttons.buttons.all { it is InlineUrlReplyKeyboard && it.url != null } -> {
                    val text = buildString {
                        append(request.text)
                        append("\n\n")
                        request.buttons.buttons.forEach { button ->
                            button as InlineUrlReplyKeyboard
                            append("${button.text}: ${button.url}")
                        }
                    }
                    add("Body", text)
                    log.info("TEST 2\n\n$text")
                }

                request.buttons is MarkupInlinedReplyKeyboard -> {
                    val contentSid = when (request.buttons.buttons.size) {
                        1 -> "HXf734048374f583cb6f860ba10776a3c8"
                        2 -> "HX26a343ae652005995d8e6393666583ac"
                        3 -> "HXc997d17edaa9d731a45e4d26fa5feb4b"
                        4 -> "HX841de4bdaa941dedcf9a74dbcf8ed628"
                        5 -> "HXcd1cc7e45a6ba157ac128a00417bb4ff"
                        6 -> "HX41433473e50e8e01218f20f7a25a62de"
                        7 -> "HX85cabf5052512c51efecd7711dbe8cee"
                        else -> "HXed7e3db98e1de585f9f631458306fe21"
                    }

                    val variables = buildString {
                        append("{\"1\":\"${request.text}\"")
                        append(", \"2\":\"Choose action\"")
                        request.buttons.buttons.forEachIndexed { index, button ->
                            button as InlineUrlReplyKeyboard
                            append(", \"${index + 3}\":\"${button.text.take(LIST_ITEM_MAX_LENGTH)}\"")
                        }
                        append("}")
                    }

                    val attributes = request.buttons.buttons
                        .withIndex()
                        .joinToString(prefix = "{", postfix = "}") { (index, button) ->
                            button as InlineUrlReplyKeyboard
                            "\"${index + 3}\":\"${button.callbackData ?: ""}\""
                        }

                    add("ContentSid", contentSid)
                    add("ContentVariables", variables.replace("\\r?\\n|\\r".toRegex(), "  "))
                    add("Attributes", attributes)
                    log.info("TEST 3\n\n$contentSid\n\n$variables\n\n$attributes")
                }

                request.buttons is MarkupReplyKeyboard -> {
                    val contentSid = when (request.buttons.buttons.size) {
                        1 -> "HXf734048374f583cb6f860ba10776a3c8"
                        2 -> "HX26a343ae652005995d8e6393666583ac"
                        3 -> "HXc997d17edaa9d731a45e4d26fa5feb4b"
                        4 -> "HX841de4bdaa941dedcf9a74dbcf8ed628"
                        5 -> "HXcd1cc7e45a6ba157ac128a00417bb4ff"
                        6 -> "HX41433473e50e8e01218f20f7a25a62de"
                        7 -> "HX85cabf5052512c51efecd7711dbe8cee"
                        else -> "HXed7e3db98e1de585f9f631458306fe21"
                    }

                    val variables = buildString {
                        append("{\"1\":\"${request.text}\"")
                        append(", \"2\":\"Choose action\"")
                        request.buttons.buttons.forEachIndexed { index, button ->
                            button as InlineUrlReplyKeyboard
                            append(", \"${index + 3}\":\"${button.text.take(LIST_ITEM_MAX_LENGTH)}\"")
                        }
                        append("}")
                    }

                    val attributes = request.buttons.buttons
                        .withIndex()
                        .joinToString(prefix = "{", postfix = "}") { (index, button) ->
                            button as InlineUrlReplyKeyboard
                            "\"${index + 3}\":\"${button.callbackData ?: ""}\""
                        }

                    add("ContentSid", contentSid)
                    add("ContentVariables", variables.replace("\\r?\\n|\\r".toRegex(), "  "))
                    add("Attributes", attributes)
                    log.info("TEST 4\n\n$contentSid\n\n$variables\n\n$attributes")
                }

                else -> {
                    add("Body", request.text)
                    log.info("TEST 5\n\n${request.text}")
                }
            }
        }

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
        val mediaSid = uploadMedia("picture.png", request.photo, "image/png")

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