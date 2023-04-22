package ua.blink.telegraff.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
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
        userId = 0L,
        firstName = "",
        lastName = null,
        phoneNumber = null
    )

    constructor(phoneNumber: String) : this(
        userId = 0L,
        firstName = "",
        lastName = null,
        phoneNumber = phoneNumber
    )

    fun getUserFullName(): String = firstName.plus(
        if (lastName?.isNotEmpty() == true) {
            " $lastName"
        } else {
            ""
        }
    )
}