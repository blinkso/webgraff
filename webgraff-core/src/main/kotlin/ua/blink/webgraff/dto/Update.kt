package ua.blink.webgraff.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

data class Update(
    @JsonProperty("MessagingServiceSid")
    val messagingServiceSid: String?,

    @JsonProperty("EventType")
    val eventType: String?,

    @JsonProperty("Attributes")
    val attributes: String?,

    @JsonProperty("Media")
    val media: String?,

    @JsonProperty("DateCreated")
    val dateCreated: String?,

    @JsonProperty("Index")
    val index: Int?,

    @JsonProperty("ChatServiceSid")
    val chatServiceSid: String?,

    @JsonProperty("MessageSid")
    val messageSid: String?,

    @JsonProperty("AccountSid")
    val accountSid: String?,

    @JsonProperty("Source")
    val source: String?,

    @JsonProperty("RetryCount")
    val retryCount: Int?,

    @JsonProperty("ClientIdentity")
    val clientIdentity: String?,

    @JsonProperty("Author")
    val author: String?,

    @JsonProperty("ParticipantSid")
    val participantSid: String?,

    @JsonProperty("Body")
    val body: String?,

    @JsonProperty("ConversationSid")
    val conversationSid: String?
) {

    fun getMessage(objectMapper: ObjectMapper): Message {
        val mediaList: List<UpdateMedia>? =
            media?.let {
                runCatching {
                    objectMapper.readValue(
                        media,
                        object : TypeReference<List<UpdateMedia>>() {})
                }.getOrNull()
            }
        
        // Debug logging to trace the values being used
        println("DEBUG - Update values being mapped to Message:")
        println("messageSid: $messageSid")
        println("accountSid: $accountSid")
        println("messagingServiceSid: $messagingServiceSid")
        println("conversationSid: $conversationSid")
        println("author: $author")
        println("attributes: $attributes")
        println("dateCreated: $dateCreated")
        println("body: $body")
        
        return Message(
            sid = messageSid ?: "",
            accountSid = accountSid ?: "",
            serviceSid = messagingServiceSid ?: "",
            chatId = conversationSid ?: "",
            user = author ?: clientIdentity,
            attributes = attributes,
            date = dateCreated ?: "",
            photo = mediaList?.map {
                Photo(
                    sid = it.sid ?: "",
                    filename = it.filename ?: "",
                    contentType = it.contentType ?: "",
                    size = it.size ?: 0,
                    category = ""
                )
            },
            text = body
        )
    }
}