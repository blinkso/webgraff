package me.ruslanys.telegraff.core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

data class TelegramContact(

    @JsonProperty("user_id")
    val userId: Long,

    @JsonProperty("first_name")
    val firstName: String,

    @JsonProperty("last_name")
    val lastName: String?,

    @JsonProperty("phone_number")
    val phoneNumber: String?

) {
    // This will be returned to validation block which will be responsible for parsing it back as a contact entity
    fun getContact(): String = ObjectMapper().writeValueAsString(this)
}