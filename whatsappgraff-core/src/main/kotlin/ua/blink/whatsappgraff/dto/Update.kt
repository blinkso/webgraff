package ua.blink.whatsappgraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Update(
    @JsonProperty("MessagingServiceSid")
    val messagingServiceSid: String?,

    @JsonProperty("EventType")
    val eventType: String?,

    @JsonProperty("Attributes")
    val attributes: String?,

    @JsonProperty("Media")
    val media: List<UpdateMedia>?,

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

    @JsonProperty("Author")
    val author: String?,

    @JsonProperty("ParticipantSid")
    val participantSid: String?,

    @JsonProperty("Body")
    val body: String?,

    @JsonProperty("ConversationSid")
    val conversationSid: String?
) {

    fun getMessage(): Message {
        return Message(
            sid = messageSid ?: "",
            accountSid = accountSid ?: "",
            serviceSid = chatServiceSid ?: "",
            chatId = conversationSid ?: "",
            user = author,
            date = dateCreated ?: "",
            photo = media?.map {
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