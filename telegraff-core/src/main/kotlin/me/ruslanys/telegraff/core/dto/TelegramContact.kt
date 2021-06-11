package me.ruslanys.telegraff.core.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

data class TelegramContact(
    @get:JsonProperty("user_id")
    val userId: Long,

    @get:JsonProperty("first_name")
    val firstName: String,

    @get:JsonProperty("last_name")
    val lastName: String?,

    @get:JsonProperty("phone_number")
    val phoneNumber: String?
) {
    constructor() : this(
        0L,
        "",
        null,
        null
    )

    // This will be returned to validation block which will be responsible for parsing it back as a contact entity
    fun getContact(): String? {
        return ObjectMapper().writeValueAsString(this)
    }
}