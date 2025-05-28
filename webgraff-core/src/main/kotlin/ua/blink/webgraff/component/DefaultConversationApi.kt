package ua.blink.webgraff.component

import com.twilio.jwt.accesstoken.AccessToken
import com.twilio.jwt.accesstoken.ChatGrant
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.CodecConfigurer
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import ua.blink.webgraff.dto.Conversation
import ua.blink.webgraff.dto.Media
import ua.blink.webgraff.dto.Message
import ua.blink.webgraff.dto.Response
import ua.blink.webgraff.dto.WebChatTokenResponse
import ua.blink.webgraff.dto.request.DocumentSendRequest
import ua.blink.webgraff.dto.request.MessageSendRequest
import ua.blink.webgraff.dto.request.PhotoSendRequest
import ua.blink.webgraff.dto.request.VoiceSendRequest
import ua.blink.webgraff.util.FileType
import ua.blink.webgraff.util.ImageType
import java.net.URLDecoder
import java.time.Duration
import java.time.Instant
import java.util.*


class DefaultConversationApi(
    private val apiKeySid: String,
    private val apiKeySecret: String,
    private val accountSid: String,
    private val serviceSid: String,
    private val flexFlowSid: String,
    private val buttonTemplate: List<String>,
    private val listTemplate: List<String>,
    private val tokenTtl: Int
) : ConversationApi {

    private val log = LoggerFactory.getLogger(DefaultConversationApi::class.java)
    private val REQUEST_TIMEOUT_SECONDS = 30L

    private val restTemplate = WebClient.builder()
        .baseUrl("https://conversations.twilio.com/v1")
        .defaultHeader(
            HttpHeaders.AUTHORIZATION,
            "Basic " + Base64.getEncoder().encodeToString("$accountSid:$apiKeySecret".toByteArray())
        )
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs(this::configureCodecs)
                .build()
        )
        .build()
        
    private val fileRestTemplate = WebClient.builder()
        .baseUrl("https://mcs.us1.twilio.com/v1")
        .defaultHeader(
            HttpHeaders.AUTHORIZATION,
            "Basic " + Base64.getEncoder().encodeToString("$accountSid:$apiKeySecret".toByteArray())
        )
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs(this::configureCodecs)
                .build()
        )
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create()
                    .followRedirect(true) // Configure to follow redirects
            )
        )
        .build()

    private fun configureCodecs(configurer: CodecConfigurer) {
        configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) // 16MB
    }

    private fun ClientResponse.handleError(contextInfo: String): Mono<Throwable> {
        return this.bodyToMono(String::class.java)
            .flatMap { body ->
                log.error("API Error: {} - {}", contextInfo, body)
                Mono.error(RuntimeException("API Error: $contextInfo - $body"))
            }
    }

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
            allMessages.addAll(getMessages(conversation.chatId))
        }

        return allMessages.filter { it.user != "system" }
    }

    override fun getMessages(chatId: String): List<Message> {
        val messagesResponse = restTemplate.get()
            .uri("/Services/$serviceSid/Conversations/${chatId}/Messages?Page=0&PageSize=5&Order=desc")
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("getUpdates/Services/$serviceSid/Conversations/${chatId}/Messages") }
            )
            .bodyToMono(Response::class.java)
            .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))

        return messagesResponse?.messages?.reversed() ?: listOf()
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
            .uri("/Services/$serviceSid/Configuration/Webhooks")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Set content type to form urlencoded
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("setWebhook($url)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<String>() {})
            .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
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
            .toFuture()
            .get()
    }

    private fun formMessageData(request: MessageSendRequest): MultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            request.formBody()?.let { body ->
                add("Body", body)
            }
            request.formAttributes()?.let { attributes ->
                add("Attributes", attributes)
            }
            request.formContent(
                buttonTemplate = buttonTemplate,
                listTemplate = listTemplate
            )?.let { contentPair ->
                add("ContentSid", contentPair.first)
                add("ContentVariables", contentPair.second)
            }
            request.formShortenUrls()?.let { shortenUrls ->
                add("ShortenUrls", shortenUrls.toString())
            }
            add("Author", "system") // Sends as the system user
            log.info("MessageBodyContent: $this")
        }
    }

    override fun sendDocument(request: DocumentSendRequest): Message {
        val fileType =
            FileType.fromByteArray(request.document)
        val mediaSid =
            uploadMedia("${request.name}.${fileType.extension}", request.document, fileType.type)

        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("Body", (request.caption ?: ""))
            add("MediaSid", mediaSid)
            add("Author", "system") // Sends as the system user
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
            add("Author", "system") // Sends as the system user
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
            .toFuture()
            .get()
    }

    override fun sendVoice(request: VoiceSendRequest): Message {
        val fileType =
            FileType.fromByteArray(request.voice)
        val mediaSid =
            uploadMedia("voice${Instant.now().toEpochMilli()}.${fileType.extension}", request.voice, fileType.type)

        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("Body", (request.caption ?: ""))
            add("MediaSid", mediaSid)
            add("Author", "system") // Sends as the system user
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
            .toFuture()
            .get()
    }

    private fun uploadMedia(filename: String, fileContent: ByteArray, contentType: String): String {
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("MediaStream", fileContent).header("Content-Type", contentType)

        val responseMedia = fileRestTemplate.post()
            .uri("/Services/$serviceSid/Media")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("uploadFile($filename)") }
            )
            .bodyToMono(object : ParameterizedTypeReference<Media>() {})
            .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))

        return responseMedia?.sid ?: throw RuntimeException("Failed to upload media")
    }
    
    /**
     * Internal method to generate JWT tokens for Twilio API authentication.
     * Used by the framework for server-side API calls.
     */
    private fun generateToken(identity: String): String {
        log.info("Generating token for identity: {}", identity)
        
        val chatGrant = ChatGrant()
        chatGrant.serviceSid = serviceSid
        
        val token = AccessToken.Builder(accountSid, apiKeySid, apiKeySecret)
            .identity(identity)
            .ttl(tokenTtl)
            .grant(chatGrant)
            .build()
        
        return token.toJwt()
    }
    
    override fun createConversation(identity: String, attributes: String?): Conversation {
        log.info("Creating conversation for identity: {}", identity)
        
        // Server-side token is still needed for API authentication
        val token = generateToken(identity)
        
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("FriendlyName", "Conversation with $identity")
            if (attributes != null) {
                add("Attributes", attributes)
            }
        }
        
        // Create the conversation
        val conversation = restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("createConversation($identity)") }
            )
            .bodyToMono(Conversation::class.java)
            .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            ?: throw RuntimeException("Failed to create conversation")
        
        // Add the participant to the conversation
        val participantFormData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("Identity", identity)
        }
        
        restTemplate
            .post()
            .uri("/Services/$serviceSid/Conversations/${conversation.chatId}/Participants")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(participantFormData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("addParticipant($identity, ${conversation.chatId})") }
            )
            .bodyToMono(Object::class.java)
            .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
        
        // Create a task in Flex to route this conversation to an agent
        createFlexTask(conversation.chatId, identity, attributes)
        
        return conversation
    }
    
    private fun createFlexTask(conversationSid: String, identity: String, attributes: String?) {
        log.info("Creating Flex task for conversation: {}", conversationSid)
        
        val taskAttributes = """
            {
                "customerName": "$identity",
                "channelType": "web",
                "conversationSid": "$conversationSid",
                "flexFlowSid": "$flexFlowSid"
                ${attributes?.let { ", \"customAttributes\": $it" } ?: ""}
            }
        """.trimIndent()
        
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("WorkflowSid", flexFlowSid)
            add("TaskChannel", "chat")
            add("Attributes", taskAttributes)
        }
        
        val taskServiceUrl = "https://taskrouter.twilio.com/v1/Workspaces/"
        
        WebClient.builder()
            .baseUrl(taskServiceUrl)
            .defaultHeader(
                HttpHeaders.AUTHORIZATION,
                "Basic " + Base64.getEncoder().encodeToString("$accountSid:$apiKeySecret".toByteArray())
            )
            .build()
            .post()
            .uri("/<workspace_sid>/Tasks") // Replace with your Workspace SID in configuration
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus(
                { status -> status.isError },
                { clientResponse -> clientResponse.handleError("createFlexTask($conversationSid)") }
            )
            .bodyToMono(Object::class.java)
            .block(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
    }
}