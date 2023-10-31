package ua.blink.whatsappgraff.component

import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.ByteArrayResource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import ua.blink.whatsappgraff.dto.File
import ua.blink.whatsappgraff.dto.Message
import ua.blink.whatsappgraff.dto.Response
import ua.blink.whatsappgraff.dto.Update
import ua.blink.whatsappgraff.dto.request.*
import java.time.Duration

class DefaultConversationApi(
    accessKey: String,
    accountSid: String,
) : ConversationApi {

    private val restTemplate = WebClient.builder()
        .baseUrl("https://api.telegram.org/bot$accessKey")
        .build()
    private val fileRestTemplate = WebClient.builder()
        .baseUrl("https://api.telegram.org/file/bot$accessKey")
        .build()

    override fun getUpdates(offset: Long?, timeout: Int?): List<Update> {
        val params = hashMapOf<String, Any>()
        offset?.let { params["offset"] = it }
        timeout?.let { params["timeout"] = it }

        return restTemplate
            .post()
            .uri("/getUpdates")
            .body(BodyInserters.fromValue(params))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("getUpdates($offset $timeout)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Response<List<Update>>>() {})
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
            .result!!
    }

    override fun getFile(fileId: String): File {
        val params = hashMapOf("file_id" to fileId)

        return restTemplate
            .post()
            .uri("/getFile")
            .body(BodyInserters.fromValue(params))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("getFile($fileId)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Response<File>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
            .result!!
    }

    override fun getFileByPath(filePath: String): ByteArray {
        val params = mapOf<String, String>()

        return fileRestTemplate.get()
            .uri("/$filePath", params)
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

    override fun setWebhook(url: String): Boolean {
        val params = hashMapOf("url" to url)

        return restTemplate
            .post()
            .uri("/setWebhook")
            .body(BodyInserters.fromValue(params))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("setWebhook($url)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Response<Boolean>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
            .result!!
    }

    override fun removeWebhook(): Boolean {
        return setWebhook("")
    }

    override fun sendMessage(request: MessageSendRequest): Message {
        return restTemplate
            .post()
            .uri("/sendMessage")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendMessage($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Response<Message>>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
            .result!!
    }

    override fun sendDocument(request: DocumentSendRequest): Message {
        val formData = createFormData(request).apply {
            add("document", object : ByteArrayResource(request.document) {
                override fun getFilename(): String {
                    return request.name
                }
            })
        }

        return restTemplate
            .post()
            .uri("/sendDocument")
            .body(BodyInserters.fromMultipartData(formData))
//            .headers { headers ->
//                headers.contentType = MediaType.MULTIPART_FORM_DATA
//            }
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendDocument($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Response<Message>>() {})
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
            .result!!
    }

    override fun sendPhoto(request: PhotoSendRequest): Message {
        val formData = createFormData(request).apply {
            add("photo", object : ByteArrayResource(request.photo) {
                override fun getFilename(): String {
                    return "picture.png"
                }
            })
        }

        return restTemplate
            .post()
            .uri("/sendPhoto")
            .body(BodyInserters.fromMultipartData(formData))
//            .headers { headers ->
//                headers.contentType = MediaType.MULTIPART_FORM_DATA
//            }
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendPhoto($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Response<Message>>() {})
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
            .result!!
    }

    override fun sendVoice(request: VoiceSendRequest): Message {
        val formData = createFormData(request).apply {
            add("voice", object : ByteArrayResource(request.voice) {
                override fun getFilename(): String {
                    return "voice.mp3"
                }
            })
        }

        return restTemplate
            .post()
            .uri("/sendVoice")
            .body(BodyInserters.fromMultipartData(formData))
//            .headers { headers ->
//                headers.contentType = MediaType.MULTIPART_FORM_DATA
//            }
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("sendVoice($request)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Response<Message>>() {})
            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(MIN_BACKOFF_SECONDS)))
            .toFuture()
            .get()
            .result!!
    }

    private fun createFormData(request: MediaSendRequest): LinkedMultiValueMap<String, Any> =
        LinkedMultiValueMap<String, Any>().apply {
            add("chat_id", request.chatId)
            add("reply_markup", request.replyKeyboard)
            add("disable_notification", request.disableNotification)
            request.caption?.let { add("caption", request.caption) }
//            request.parseMode?.let { add("parse_mode", request.parseMode.name) }
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