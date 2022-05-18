package me.ruslanys.telegraff.core.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TelegramMessage(
    @JsonProperty("message_id")
    val id: Long,

    @JsonProperty("from")
    val user: TelegramUser?,

    @JsonProperty("date")
    val date: Long,

    @JsonProperty("chat")
    val chat: TelegramChat,

    @JsonProperty("contact")
    val contact: TelegramContact?,

    @JsonProperty("photo")
    val photo: List<TelegramPhoto>?,

    @JsonProperty("text")
    var text: String?,

    // Used for callback populating and filtering
    @JsonProperty("callback_query")
    var callbackQuery: TelegramCallbackQuery?
) {

    // In case if it was a contact request question phone number of user contact will be returned and validation block will receive telegram contact
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