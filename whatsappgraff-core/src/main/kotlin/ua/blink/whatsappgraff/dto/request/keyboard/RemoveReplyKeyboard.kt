package ua.blink.whatsappgraff.dto.request.keyboard

import com.fasterxml.jackson.annotation.JsonProperty

class RemoveReplyKeyboard : ReplyKeyboard() {

    @JsonProperty("remove_keyboard")
    fun getRemoveKeyboard(): Boolean {
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RemoveReplyKeyboard) return false
        if (!super.equals(other)) return false
        return true
    }

    @Suppress("RedundantOverride")
    override fun hashCode(): Int {
        return super.hashCode()
    }


}