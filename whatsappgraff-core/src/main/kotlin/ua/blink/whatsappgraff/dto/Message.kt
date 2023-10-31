package ua.blink.whatsappgraff.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class Message(
    @JsonProperty("message_id")
    val id: Long,

    @JsonProperty("from")
    val user: User?,

    @JsonProperty("date")
    val date: Long,

    @JsonProperty("chat")
    val chat: Chat,

    @JsonProperty("contact")
    val contact: Contact?,

    @JsonProperty("photo")
    val photo: List<Photo>?,

    @JsonProperty("text")
    var text: String?,
) {

    // In case if it was a contact request question phone number of user contact will be returned and validation block will receive contact
    // for it's validation
    fun getMessageText(): String? {
        return if (contact != null) {
            contact.phoneNumber ?: text
        } else if (photo != null) {
            photo.firstOrNull()?.id ?: text
        } else {
            text
        }
    }
}