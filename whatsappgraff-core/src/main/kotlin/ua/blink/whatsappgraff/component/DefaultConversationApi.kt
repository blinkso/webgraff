package ua.blink.whatsappgraff.component

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import ua.blink.whatsappgraff.dto.Conversation
import ua.blink.whatsappgraff.dto.Message
import ua.blink.whatsappgraff.dto.request.DocumentSendRequest
import ua.blink.whatsappgraff.dto.request.MessageSendRequest
import ua.blink.whatsappgraff.dto.request.PhotoSendRequest
import ua.blink.whatsappgraff.dto.request.VoiceSendRequest
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

    override fun getFileByPath(filePath: String): ByteArray {
        return restTemplate.get()
            .uri("/Services/$serviceSid/Conversations/Media/$filePath")
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
        return restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations/${request.chatId}/Messages")
            .body(BodyInserters.fromValue(request))
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

    override fun getUpdates(offset: String?, timeout: Int?): List<Message> {
        val conversationsFlux = restTemplate.get()
            .uri("/Services/$serviceSid/Conversations")
            .retrieve()
            .bodyToFlux(Conversation::class.java)
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))

        val messagesFlux = conversationsFlux.flatMap { conversation ->
            val uri = if (offset != null) {
                "/Services/$serviceSid/Conversations/${conversation.chatId}/Messages?After=$offset"
            } else {
                "/Services/$serviceSid/Conversations/${conversation.chatId}/Messages"
            }
            restTemplate.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Message::class.java)
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
        }

        return messagesFlux
            .collectList()
            .block(Duration.ofSeconds(timeout?.toLong() ?: REQUEST_TIMEOUT_SECONDS))
            ?: emptyList()
    }

    override fun sendDocument(request: DocumentSendRequest): Message {
        val mediaSid = uploadMedia(request.name, request.document, "application/octet-stream")

        val params = hashMapOf(
            "Body" to "", // The body can be empty since we're sending media
            "MediaSid" to mediaSid
        )

        return restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations/${request.chatId}/Messages")
            .body(BodyInserters.fromValue(params))
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

        val params = hashMapOf(
            "Body" to "", // The body can be empty since we're sending media
            "MediaSid" to mediaSid
        )

        return restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations/${request.chatId}/Messages")
            .body(BodyInserters.fromValue(params))
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

        val params = hashMapOf(
            "Body" to "", // The body can be empty since we're sending media
            "MediaSid" to mediaSid
        )

        return restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations/${request.chatId}/Messages")
            .body(BodyInserters.fromValue(params))
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

        return restTemplate
            .post()
            .uri("/Services/$serviceSid/Media")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(formData))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Message>() {})
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