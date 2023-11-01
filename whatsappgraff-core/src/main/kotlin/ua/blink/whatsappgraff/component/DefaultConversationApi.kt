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
import ua.blink.whatsappgraff.dto.request.keyboard.CancelReplyKeyboard
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
            var text = ""
            text += request.text
            (request.buttons as? MarkupReplyKeyboard)?.buttons?.forEach { button ->
                text += "\n${(button as? InlineUrlReplyKeyboard)?.text} ${(button as? InlineUrlReplyKeyboard)?.url} ${(button as? InlineUrlReplyKeyboard)?.text}"
            }
            (request.buttons as? MarkupInlinedReplyKeyboard)?.buttons?.forEach { button ->
                text += "\n${(button as? InlineUrlReplyKeyboard)?.text} ${(button as? InlineUrlReplyKeyboard)?.url} ${(button as? InlineUrlReplyKeyboard)?.text}"
            }
            (request.buttons as? CancelReplyKeyboard)?.buttons?.forEach { button ->
                text += "\n${(button as? InlineUrlReplyKeyboard)?.text} ${(button as? InlineUrlReplyKeyboard)?.url} ${(button as? InlineUrlReplyKeyboard)?.text}"
            }
            add("Body", text)
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
        private const val MIN_BACKOFF_SECONDS = 2L
        private const val MAX_RETRY_ATTEMPTS = 3L
    }
}